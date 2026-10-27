"""
Canonical label taxonomy for AgriPlant AI.

PlantVillage and PlantDoc name the same disease differently, and Kaggle
mirrors of PlantVillage disagree on punctuation (trailing underscores,
"Tomato___Tomato_mosaic_virus" vs "Tomato_mosaic_virus", etc). Training on
both datasets without reconciling this silently produces duplicate classes
for the same disease, which wrecks accuracy in a way that's hard to spot.

So: one canonical name per disease, PlantVillage-style, and a normalizer
that maps messy real folder names onto it. Canonical names are also what
goes in MySQL `diseases.class_label`, so inference output joins straight
to a row.
"""
import re

# ---------------------------------------------------------------------------
# Canonical classes. PlantVillage covers all 38; the `plantdoc` field marks
# which also have real-world (non-lab) images available.
# ---------------------------------------------------------------------------
CANONICAL = [
    "Apple___Apple_scab",
    "Apple___Black_rot",
    "Apple___Cedar_apple_rust",
    "Apple___healthy",
    "Blueberry___healthy",
    "Cherry___Powdery_mildew",
    "Cherry___healthy",
    "Corn___Cercospora_leaf_spot_Gray_leaf_spot",
    "Corn___Common_rust",
    "Corn___Northern_Leaf_Blight",
    "Corn___healthy",
    "Grape___Black_rot",
    "Grape___Esca_Black_Measles",
    "Grape___Leaf_blight_Isariopsis_Leaf_Spot",
    "Grape___healthy",
    "Orange___Haunglongbing_Citrus_greening",
    "Peach___Bacterial_spot",
    "Peach___healthy",
    "Pepper_bell___Bacterial_spot",
    "Pepper_bell___healthy",
    "Potato___Early_blight",
    "Potato___Late_blight",
    "Potato___healthy",
    "Raspberry___healthy",
    "Soybean___healthy",
    "Squash___Powdery_mildew",
    "Strawberry___Leaf_scorch",
    "Strawberry___healthy",
    "Tomato___Bacterial_spot",
    "Tomato___Early_blight",
    "Tomato___Late_blight",
    "Tomato___Leaf_Mold",
    "Tomato___Septoria_leaf_spot",
    "Tomato___Spider_mites_Two_spotted_spider_mite",
    "Tomato___Target_Spot",
    "Tomato___Tomato_Yellow_Leaf_Curl_Virus",
    "Tomato___Tomato_mosaic_virus",
    "Tomato___healthy",
]

# PlantDoc folder name -> canonical. PlantDoc marks healthy classes by
# using the bare plant name ("grape leaf" means a healthy grape leaf).
PLANTDOC_MAP = {
    "Apple Scab Leaf":            "Apple___Apple_scab",
    "Apple leaf":                 "Apple___healthy",
    "Apple rust leaf":            "Apple___Cedar_apple_rust",
    "Bell_pepper leaf":           "Pepper_bell___healthy",
    "Bell_pepper leaf spot":      "Pepper_bell___Bacterial_spot",
    "Blueberry leaf":             "Blueberry___healthy",
    "Cherry leaf":                "Cherry___healthy",
    "Corn Gray leaf spot":        "Corn___Cercospora_leaf_spot_Gray_leaf_spot",
    "Corn leaf blight":           "Corn___Northern_Leaf_Blight",
    "Corn rust leaf":             "Corn___Common_rust",
    "Peach leaf":                 "Peach___healthy",
    "Potato leaf early blight":   "Potato___Early_blight",
    "Potato leaf late blight":    "Potato___Late_blight",
    "Potato leaf":                "Potato___healthy",
    "Raspberry leaf":             "Raspberry___healthy",
    "Soyabean leaf":              "Soybean___healthy",
    "Soybean leaf":               "Soybean___healthy",
    "Squash Powdery mildew leaf": "Squash___Powdery_mildew",
    "Strawberry leaf":            "Strawberry___healthy",
    "Tomato Early blight leaf":   "Tomato___Early_blight",
    "Tomato Septoria leaf spot":  "Tomato___Septoria_leaf_spot",
    "Tomato leaf":                "Tomato___healthy",
    "Tomato leaf bacterial spot": "Tomato___Bacterial_spot",
    "Tomato leaf late blight":    "Tomato___Late_blight",
    "Tomato leaf mosaic virus":   "Tomato___Tomato_mosaic_virus",
    "Tomato leaf yellow virus":   "Tomato___Tomato_Yellow_Leaf_Curl_Virus",
    "Tomato mold leaf":           "Tomato___Leaf_Mold",
    "grape leaf":                 "Grape___healthy",
    "grape leaf black rot":       "Grape___Black_rot",
}

# Classes that exist in PlantDoc, i.e. have real-world field images.
HAS_REAL_WORLD_DATA = set(PLANTDOC_MAP.values())


def _normalize(name: str) -> str:
    """Lowercase, strip punctuation/parentheticals, collapse separators.

    'Corn_(maize)___Common_rust_' and 'Corn___Common rust' both reduce to
    'corn common rust', so mirror-specific punctuation stops mattering.
    """
    s = name.lower()
    s = s.replace("___", " ")
    s = re.sub(r"\((.*?)\)", r" \1 ", s)      # unwrap parentheticals
    s = re.sub(r"[_\-,.']", " ", s)
    s = re.sub(r"\s+", " ", s).strip()
    # common synonym collapsing across mirrors
    s = s.replace("maize", "corn")
    s = s.replace("including sour", "")
    s = s.replace("bell pepper", "pepper bell")
    s = re.sub(r"\s+", " ", s).strip()
    # Collapse repeated adjacent tokens. 'Corn_(maize)___Common_rust_'
    # unwraps to 'corn maize common rust', then maize->corn makes
    # 'corn corn common rust'; without this it would never match.
    tokens = s.split()
    deduped = [t for i, t in enumerate(tokens) if i == 0 or t != tokens[i - 1]]
    return " ".join(deduped)


# Precomputed lookup of canonical names by normalized form.
_CANON_BY_NORM = {_normalize(c): c for c in CANONICAL}

# Extra aliases for PlantVillage folder spellings that don't normalize
# cleanly onto their canonical form.
_ALIASES = {
    # Keys are already-normalized forms (lowercased, punctuation stripped,
    # adjacent duplicate words collapsed), so "Tomato___Tomato_mosaic_virus"
    # arrives here as "tomato mosaic virus".
    "tomato mosaic virus": "Tomato___Tomato_mosaic_virus",
    "tomato yellowleaf curl virus": "Tomato___Tomato_Yellow_Leaf_Curl_Virus",
    "tomato yellow leaf curl virus": "Tomato___Tomato_Yellow_Leaf_Curl_Virus",
    "tomato spider mites two spotted spider mite": "Tomato___Spider_mites_Two_spotted_spider_mite",
    "tomato spider mites": "Tomato___Spider_mites_Two_spotted_spider_mite",
    "grape esca black measles": "Grape___Esca_Black_Measles",
    "grape leaf blight isariopsis leaf spot": "Grape___Leaf_blight_Isariopsis_Leaf_Spot",
    "orange haunglongbing citrus greening": "Orange___Haunglongbing_Citrus_greening",
    "orange huanglongbing citrus greening": "Orange___Haunglongbing_Citrus_greening",
    "corn cercospora leaf spot gray leaf spot": "Corn___Cercospora_leaf_spot_Gray_leaf_spot",
    "corn gray leaf spot": "Corn___Cercospora_leaf_spot_Gray_leaf_spot",
    "pepper bell bacterial spot": "Pepper_bell___Bacterial_spot",
    "pepper bell healthy": "Pepper_bell___healthy",
    "cherry powdery mildew": "Cherry___Powdery_mildew",
    "cherry healthy": "Cherry___healthy",
}


def to_canonical(folder_name: str, dataset: str = "auto") -> str | None:
    """Map a dataset folder name to a canonical class, or None if unknown.

    Callers should treat None as an error to report, never to silently skip —
    an unmapped folder means images quietly vanish from training.
    """
    if dataset in ("plantdoc", "auto") and folder_name in PLANTDOC_MAP:
        return PLANTDOC_MAP[folder_name]

    norm = _normalize(folder_name)
    if norm in _CANON_BY_NORM:
        return _CANON_BY_NORM[norm]
    if norm in _ALIASES:
        return _ALIASES[norm]

    # Try a case-insensitive PlantDoc match before giving up.
    for k, v in PLANTDOC_MAP.items():
        if _normalize(k) == norm:
            return v
    return None


def pretty(canonical: str) -> tuple[str, str]:
    """Split a canonical label into (plant, disease) for display."""
    plant, _, disease = canonical.partition("___")
    return plant.replace("_", " ").strip(), disease.replace("_", " ").strip()


if __name__ == "__main__":
    # Sanity check: every PlantDoc target must be a real canonical class.
    unknown = sorted(set(PLANTDOC_MAP.values()) - set(CANONICAL))
    assert not unknown, f"PlantDoc maps to non-canonical classes: {unknown}"
    print(f"{len(CANONICAL)} canonical classes")
    print(f"{len(HAS_REAL_WORLD_DATA)} of them have PlantDoc (real-world) images")
    print(f"{len(CANONICAL) - len(HAS_REAL_WORLD_DATA)} are lab-only (PlantVillage only)")


# ---------------------------------------------------------------------------
# Species-level helpers.
#
# Species prediction is a much easier problem than disease prediction: a
# tomato leaf looks like a tomato leaf whether or not it has blight. Reading
# the species off the top-1 class throws that advantage away — if the model
# splits 0.3/0.3/0.3 across three tomato diseases and 0.1 on a potato class,
# top-1 might be any of them, but the species is tomato with ~0.9 certainty.
#
# So species is computed by summing probabilities across every class of the
# same plant, which is both more accurate and separately reportable.
# ---------------------------------------------------------------------------

SPECIES = sorted({c.partition("___")[0] for c in CANONICAL})

SPECIES_DISPLAY = {
    "Apple": "Apple", "Blueberry": "Blueberry", "Cherry": "Cherry",
    "Corn": "Maize / corn", "Grape": "Grape", "Orange": "Orange",
    "Peach": "Peach", "Pepper_bell": "Bell pepper", "Potato": "Potato",
    "Raspberry": "Raspberry", "Soybean": "Soybean", "Squash": "Squash",
    "Strawberry": "Strawberry", "Tomato": "Tomato",
}

SPECIES_HI = {
    "Apple": "सेब", "Blueberry": "ब्लूबेरी", "Cherry": "चेरी", "Corn": "मक्का",
    "Grape": "अंगूर", "Orange": "संतरा", "Peach": "आड़ू",
    "Pepper_bell": "शिमला मिर्च", "Potato": "आलू", "Raspberry": "रसभरी",
    "Soybean": "सोयाबीन", "Squash": "कद्दू", "Strawberry": "स्ट्रॉबेरी",
    "Tomato": "टमाटर",
}


def species_of(canonical: str) -> str:
    """Species key for a canonical class label."""
    return canonical.partition("___")[0]


def species_display(species_key: str, lang: str = "en") -> str:
    if lang == "hi":
        return SPECIES_HI.get(species_key, species_key.replace("_", " "))
    return SPECIES_DISPLAY.get(species_key, species_key.replace("_", " "))


def aggregate_species(class_names: list[str], probs) -> list[tuple[str, float]]:
    """Sum class probabilities per species, highest first.

    Returns [(species_key, probability), ...]. Accepts any sequence of floats
    so this stays importable without numpy.
    """
    totals: dict[str, float] = {}
    for name, p in zip(class_names, probs):
        key = species_of(name)
        totals[key] = totals.get(key, 0.0) + float(p)
    return sorted(totals.items(), key=lambda kv: kv[1], reverse=True)
