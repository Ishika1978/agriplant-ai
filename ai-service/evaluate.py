"""
Evaluate the trained classifier on the held-out PlantDoc (real-world) test set.

Overall accuracy hides the failures that matter: a model can score well while
being useless on the specific diseases a farmer actually reports. This prints
per-class precision/recall and the worst confusions so you can see which
classes are unreliable before shipping.

Run:
    python evaluate.py --model models/plant_disease_cnn.keras --data-dir data
"""
import argparse
import json
from pathlib import Path

import numpy as np
import tensorflow as tf
from tensorflow.keras.applications.mobilenet_v2 import preprocess_input

from labels import aggregate_species, species_of

IMG_SIZE = (224, 224)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--model", default="models/plant_disease_cnn.keras")
    ap.add_argument("--data-dir", default="data")
    ap.add_argument("--split", default="test", choices=["test", "val"])
    ap.add_argument("--batch-size", type=int, default=32)
    ap.add_argument("--top-confusions", type=int, default=15)
    args = ap.parse_args()

    split_dir = Path(args.data_dir) / args.split
    if not split_dir.exists():
        raise SystemExit(f"{split_dir} not found — run prepare_data.py first.")

    model = tf.keras.models.load_model(args.model)

    labels_path = Path(args.model).parent / "labels.json"
    train_labels = None
    if labels_path.exists():
        train_labels = json.loads(labels_path.read_text())["class_names"]

    ds = tf.keras.utils.image_dataset_from_directory(
        split_dir, labels="inferred", label_mode="categorical",
        image_size=IMG_SIZE, batch_size=args.batch_size, shuffle=False)
    class_names = ds.class_names

    if train_labels and train_labels != class_names:
        print("!! Label order mismatch between training and this split.")
        print("   Training order:", train_labels[:3], "...")
        print("   This split:    ", class_names[:3], "...")
        print("   Results below would be scrambled. Aborting.")
        raise SystemExit(1)

    ds = ds.map(lambda x, y: (preprocess_input(x), y)).prefetch(tf.data.AUTOTUNE)

    y_true, y_prob = [], []
    for xb, yb in ds:
        y_prob.append(model.predict(xb, verbose=0))
        y_true.append(yb.numpy())
    y_prob = np.concatenate(y_prob)
    y_true = np.argmax(np.concatenate(y_true), axis=1)
    y_pred = np.argmax(y_prob, axis=1)
    conf_scores = y_prob.max(axis=1)

    n = len(y_true)
    acc = float((y_true == y_pred).mean())
    top3 = float(np.mean([t in np.argsort(p)[-3:] for t, p in zip(y_true, y_prob)]))
    print(f"\n{args.split}: {n} images, {len(class_names)} classes")
    print(f"top-1 accuracy: {acc:.4f}")
    print(f"top-3 accuracy: {top3:.4f}")
    print(f"mean confidence when correct:   {conf_scores[y_true == y_pred].mean():.3f}")
    wrong = y_true != y_pred
    if wrong.any():
        print(f"mean confidence when WRONG:     {conf_scores[wrong].mean():.3f}")
        print("  (if these two are close, the confidence score shown to farmers\n"
              "   is not a usable reliability signal — consider a abstain threshold)")

    # ---- species-level accuracy ----
    # Reported separately because it is the stronger claim: getting the crop
    # right is easier than getting the disease right, and the app shows the
    # crop name to the farmer either way.
    species_true = [species_of(class_names[i]) for i in y_true]
    species_top1 = [species_of(class_names[i]) for i in y_pred]
    species_agg = [aggregate_species(class_names, p)[0][0] for p in y_prob]

    acc_sp_top1 = float(np.mean([a == b for a, b in zip(species_true, species_top1)]))
    acc_sp_agg = float(np.mean([a == b for a, b in zip(species_true, species_agg)]))
    print(f"\nspecies accuracy (from top-1 class):   {acc_sp_top1:.4f}")
    print(f"species accuracy (summed per species): {acc_sp_agg:.4f}")
    if acc_sp_agg >= acc_sp_top1:
        print("  -> aggregating beats reading species off the top-1 label, which is\n"
              "     why app.py identifies the crop that way.")

    # ---- per-class table ----
    print(f"\n{'class':<48} {'n':>4} {'prec':>6} {'recall':>7}")
    print("-" * 70)
    rows = []
    for i, c in enumerate(class_names):
        support = int((y_true == i).sum())
        tp = int(((y_pred == i) & (y_true == i)).sum())
        prec = tp / max(1, int((y_pred == i).sum()))
        rec = tp / max(1, support)
        rows.append((rec, c, support, prec))
        print(f"{c[:48]:<48} {support:>4} {prec:>6.2f} {rec:>7.2f}")

    print("\nWeakest classes by recall:")
    for rec, c, support, prec in sorted(rows)[:8]:
        if support:
            print(f"  {c:<48} recall={rec:.2f} (n={support})")

    # ---- confusions ----
    cm = np.zeros((len(class_names), len(class_names)), dtype=int)
    for t, p in zip(y_true, y_pred):
        cm[t, p] += 1
    pairs = [(cm[i, j], class_names[i], class_names[j])
             for i in range(len(class_names)) for j in range(len(class_names))
             if i != j and cm[i, j] > 0]
    pairs.sort(reverse=True)
    print(f"\nTop confusions (true -> predicted):")
    for count, t, p in pairs[: args.top_confusions]:
        print(f"  {count:>3}x  {t}  ->  {p}")

    np.savetxt(Path(args.model).parent / f"confusion_{args.split}.csv",
               cm, fmt="%d", delimiter=",",
               header=",".join(class_names), comments="")
    print(f"\nWrote confusion matrix -> {Path(args.model).parent}/confusion_{args.split}.csv")


if __name__ == "__main__":
    main()
