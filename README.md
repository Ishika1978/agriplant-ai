# 🌱 AgriPlant AI

**AI-Based Plant Identification and Disease Detection Platform**

AgriPlant AI is a web-based agricultural assistance platform designed to help users analyze plant leaf images, identify possible plant diseases, access care recommendations, and discover relevant government schemes.

The platform aims to make plant-health information more accessible through a multilingual interface supporting English and Hindi.

## ✨ Features

- **Plant Disease Detection:** Analyze uploaded leaf images and predict a supported plant or disease class.
- **Care Guidance:** Display relevant care recommendations for supported predictions.
- **Government Schemes:** Help users discover potentially relevant agricultural schemes.
- **Multilingual Interface:** Switch between English and Hindi.
- **Plant Health History:** Maintain a record of previous analyses where persistence is configured.

## 🛠️ Technology Stack

| Component | Technologies |
|---|---|
| Frontend | HTML, CSS, JavaScript |
| Backend | Java, Spring Boot, Maven |
| AI Service | Python, Flask, TensorFlow, Keras |
| Machine Learning | CNN, MobileNetV2 transfer learning |
| Database | MySQL |
| Translation | LibreTranslate or Google Cloud Translation, depending on configuration |

## 🏗️ System Architecture

1. The frontend allows users to upload a leaf image and view results.
2. The Spring Boot backend handles application requests and business logic.
3. The Python AI service processes images and performs model inference when connected.
4. MySQL stores application data according to the configured schema.
5. The recommendation and scheme modules provide relevant information according to their implemented rules and data sources.

## 📂 Project Structure

```text
agriplant-ai/
├── frontend/
├── backend/
├── ai-service/
├── database/
│   └── schema.sql
├── docs/
├── .gitignore
└── README.md
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or a compatible configured JDK
- Maven
- Python and pip
- MySQL
- Kaggle access if downloading the training datasets

### 1. Set up the database

Create the database and import the schema:

```bash
mysql -u root -p < database/schema.sql
```

Configure your local database credentials in the backend configuration.

### 2. Set up the AI service

```bash
cd ai-service
pip install -r requirements.txt
```

Follow `ai-service/TRAINING.md` for dataset preparation, model training, evaluation, and model export.

If the project requires Kaggle datasets, configure Kaggle access before downloading them.

### 3. Run the backend

Configure the required database and translation settings locally, then run:

```bash
cd backend
mvn spring-boot:run
```

The backend is configured to run on port `8080` unless its configuration specifies otherwise.

### 4. Run the frontend

Open `frontend/index.html` or serve the frontend using a local static web server. Configure the API base URL to match your backend.

### 5. Run the AI service

Follow the instructions in `ai-service/TRAINING.md` to start the Flask service and connect it to the backend.

## 🧪 Model Evaluation

The AI service includes training and evaluation scripts. Model performance should be reported using measured results on a held-out test dataset.

Evaluation metrics may include:

- Accuracy  85.40%
- Precision  86.75%
- Recall   85.40%
- F1-score  85.73%
- Confusion matrix


## ⚠️ Limitations

- Predictions are limited to the classes supported by the trained model.
- Image quality and differences between laboratory datasets and real field conditions can affect performance.
- Care recommendations and scheme information should be verified using reliable agricultural and official government sources.
- Demo functionality may differ from the fully connected backend and AI service.

## 🔮 Future Improvements

- Expand and validate the model using diverse real-world field images.
- Improve handling of uncertain predictions and unsupported diseases.
- Add verified agricultural recommendations and updated scheme information.
- Improve mobile accessibility and low-connectivity support.

## 👩‍💻 Contributors

Ishika Sahare   Isha Bhangre   Madhavi Katekhaye

