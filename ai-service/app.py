"""
AgriPlant AI — Python AI microservice
Serves the trained CNN over HTTP for the Spring Boot backend.

Endpoints:
    GET  /health    liveness + model/label status
    GET  /classes   the label set this model predicts
    POST /predict   multipart field "file" -> prediction JSON

Run:
    pip install -r requirements.txt
    python app.py
"""
import io
import json
import os

import numpy as np
from flask import Flask, jsonify, request
from PIL import Image
import tensorflow as tf
from tensorflow.keras.applications.mobilenet_v2 import preprocess_input

from labels import (HAS_REAL_WORLD_DATA, aggregate_species, pretty,
                     species_display, species_of)

app = Flask(__name__)

MODEL_PATH = os.environ.get("MODEL_PATH", "models/plant_disease_cnn.keras")
LABELS_PATH = os.environ.get("LABELS_PATH", "models/labels.json")
IMG_SIZE = (224, 224)

# Below this top-1 probability the model is guessing. Better to tell a farmer
# "unclear, try another photo" than to name a disease at 20% confidence and
# have them spray for it.
MIN_CONFIDENCE = float(os.environ.get("MIN_CONFIDENCE", "0.45"))

# Species is a separate, easier judgement than disease, so it gets its own
# (higher) bar. Below this the photo probably isn't one of the 14 trained
# crops at all — a houseplant, a weed, a blurry close-up of soil — and the
# right answer is "I don't recognise this plant", not a forced guess.
MIN_SPECIES_CONFIDENCE = float(os.environ.get("MIN_SPECIES_CONFIDENCE", "0.55"))

_model = None
_class_names = None


def get_class_names():
    """Label order is read from the file train.py wrote next to the model.

    Never re-derive this from a directory listing at serving time — if the
    order differs from training by even one class, every prediction is
    mislabelled while still looking perfectly plausible.
    """
    global _class_names
    if _class_names is None:
        if not os.path.exists(LABELS_PATH):
            raise FileNotFoundError(
                f"{LABELS_PATH} missing. It is written by train.py alongside the "
                "model and defines the class index order."
            )
        with open(LABELS_PATH, encoding="utf-8") as f:
            _class_names = json.load(f)["class_names"]
    return _class_names


def get_model():
    global _model
    if _model is None:
        if not os.path.exists(MODEL_PATH):
            raise FileNotFoundError(
                f"No trained model at {MODEL_PATH}. Run:\n"
                "  python prepare_data.py --raw-dir raw --out-dir data\n"
                "  python train.py --data-dir data"
            )
        _model = tf.keras.models.load_model(MODEL_PATH)
        n_out = _model.output_shape[-1]
        n_labels = len(get_class_names())
        if n_out != n_labels:
            raise ValueError(
                f"Model outputs {n_out} classes but {LABELS_PATH} lists "
                f"{n_labels}. These must match — retrain or fix the labels file."
            )
    return _model


def preprocess(image_bytes):
    img = Image.open(io.BytesIO(image_bytes)).convert("RGB").resize(IMG_SIZE)
    arr = np.asarray(img, dtype=np.float32)
    # Must match training exactly: MobileNetV2 expects [-1, 1], not [0, 1].
    # Using rescale=1/255 here instead would quietly degrade accuracy.
    return preprocess_input(np.expand_dims(arr, axis=0))


@app.route("/health", methods=["GET"])
def health():
    status = {"status": "ok", "model_path": MODEL_PATH}
    try:
        status["classes"] = len(get_class_names())
    except FileNotFoundError as e:
        status.update(status="degraded", error=str(e))
    status["model_loaded"] = _model is not None
    return jsonify(status)


@app.route("/classes", methods=["GET"])
def classes():
    try:
        names = get_class_names()
    except FileNotFoundError as e:
        return jsonify({"error": str(e)}), 503
    return jsonify({
        "count": len(names),
        "classes": [
            {
                "class_label": c,
                "plant": pretty(c)[0],
                "disease": pretty(c)[1] or "healthy",
                "trained_on_real_world_images": c in HAS_REAL_WORLD_DATA,
            }
            for c in names
        ],
    })


@app.route("/predict", methods=["POST"])
def predict():
    if "file" not in request.files:
        return jsonify({"error": "multipart field 'file' is required"}), 400

    image_bytes = request.files["file"].read()
    if not image_bytes:
        return jsonify({"error": "empty file"}), 400

    try:
        model = get_model()
        class_names = get_class_names()
    except (FileNotFoundError, ValueError) as e:
        return jsonify({"error": str(e)}), 503

    try:
        batch = preprocess(image_bytes)
    except Exception:
        return jsonify({"error": "could not decode image"}), 400

    probs = model.predict(batch, verbose=0)[0]
    order = np.argsort(probs)[::-1]
    top_idx = int(order[0])
    class_label = class_names[top_idx]
    confidence = float(probs[top_idx])
    disease = pretty(class_label)[1]

    # --- species, aggregated across all classes of each plant --------------
    species_ranked = aggregate_species(class_names, probs)
    species_key, species_conf = species_ranked[0]
    species_ok = species_conf >= MIN_SPECIES_CONFIDENCE

    # If the species is confident but the disease isn't, the top-1 class may
    # belong to a different plant than the aggregate winner. Prefer the
    # aggregate: it's the more reliable of the two signals.
    if species_ok and species_of(class_label) != species_key:
        for i in order:
            if species_of(class_names[int(i)]) == species_key:
                top_idx = int(i)
                class_label = class_names[top_idx]
                confidence = float(probs[top_idx])
                disease = pretty(class_label)[1]
                break

    return jsonify({
        # ---- plant / crop identification ----
        "plant": species_display(species_key, "en") if species_ok else None,
        "plant_hi": species_display(species_key, "hi") if species_ok else None,
        "plant_key": species_key if species_ok else None,
        "plant_confidence": round(species_conf * 100, 2),
        # False means the photo is probably not one of the 14 trained crops.
        "plant_recognised": bool(species_ok),
        "plant_alternatives": [
            {"plant": species_display(k, "en"), "confidence": round(v * 100, 2)}
            for k, v in species_ranked[1:4] if v > 0.01
        ],

        # ---- disease ----
        "class_label": class_label,
        "disease": disease or "healthy",
        "confidence": round(confidence * 100, 2),
        # The backend should surface an "unclear result" message rather than a
        # disease name when this is false.
        "reliable": bool(confidence >= MIN_CONFIDENCE and species_ok),
        # Lab-only classes have never been validated on a real field photo.
        "trained_on_real_world_images": class_label in HAS_REAL_WORLD_DATA,
        "alternatives": [
            {
                "class_label": class_names[int(i)],
                "confidence": round(float(probs[int(i)]) * 100, 2),
            }
            for i in order[1:4]
        ],
    })


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.environ.get("PORT", 5000)))
