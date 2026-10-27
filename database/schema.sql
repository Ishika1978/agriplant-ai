-- AgriPlant AI — MySQL schema
-- Matches the architecture in the project brief:
-- users, plants, predictions, diseases, care_guides, schemes, scheme_eligibility, health_history

CREATE DATABASE IF NOT EXISTS agriplant_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agriplant_ai;

-- ---------------------------------------------------------------
-- Users (farmers / gardeners)
-- ---------------------------------------------------------------
CREATE TABLE users (
    user_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(120) NOT NULL,
    phone_number    VARCHAR(20)  UNIQUE,
    email           VARCHAR(150) UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    preferred_lang  ENUM('en','hi') NOT NULL DEFAULT 'en',
    state           VARCHAR(80),
    landholding     ENUM('small','medium','large') DEFAULT 'small',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------
-- Plant species catalogue
-- ---------------------------------------------------------------
CREATE TABLE plant_species (
    species_id      INT AUTO_INCREMENT PRIMARY KEY,
    name_en         VARCHAR(80) NOT NULL,
    name_hi         VARCHAR(80) NOT NULL,
    category        ENUM('foodgrain','horticulture','cash_crop') NOT NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------
-- Disease catalogue (bilingual, one row per CNN output class)
-- ---------------------------------------------------------------
CREATE TABLE diseases (
    disease_id      INT AUTO_INCREMENT PRIMARY KEY,
    species_id      INT NOT NULL,
    class_label     VARCHAR(120) NOT NULL COMMENT 'raw CNN output class, e.g. Tomato___Early_blight',
    name_en         VARCHAR(120) NOT NULL,
    name_hi         VARCHAR(120) NOT NULL,
    description_en  TEXT,
    description_hi  TEXT,
    severity        ENUM('none','moderate','severe') NOT NULL DEFAULT 'moderate',
    FOREIGN KEY (species_id) REFERENCES plant_species(species_id) ON DELETE CASCADE,
    UNIQUE KEY uq_class_label (class_label)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------
-- Care guidance per disease
-- ---------------------------------------------------------------
CREATE TABLE care_guides (
    care_id         INT AUTO_INCREMENT PRIMARY KEY,
    disease_id      INT NOT NULL,
    water_en        VARCHAR(255), water_hi        VARCHAR(255),
    sunlight_en     VARCHAR(255), sunlight_hi     VARCHAR(255),
    fertilizer_en   VARCHAR(255), fertilizer_hi   VARCHAR(255),
    next_step_en    VARCHAR(255), next_step_hi    VARCHAR(255),
    FOREIGN KEY (disease_id) REFERENCES diseases(disease_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------
-- Predictions — every CNN inference result, tied to a user and photo
-- ---------------------------------------------------------------
CREATE TABLE predictions (
    prediction_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    species_id      INT NOT NULL,
    disease_id      INT NOT NULL,
    image_path      VARCHAR(255) NOT NULL COMMENT 'stored path/URL of the uploaded leaf photo',
    confidence      DECIMAL(5,2) NOT NULL COMMENT 'model confidence, 0-100',
    model_version   VARCHAR(40) DEFAULT 'cnn-v1',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (species_id) REFERENCES plant_species(species_id),
    FOREIGN KEY (disease_id) REFERENCES diseases(disease_id),
    INDEX idx_predictions_user_date (user_id, created_at)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------
-- Government schemes catalogue
-- ---------------------------------------------------------------
CREATE TABLE schemes (
    scheme_id       INT AUTO_INCREMENT PRIMARY KEY,
    code            VARCHAR(40) NOT NULL UNIQUE,
    name_en         VARCHAR(150) NOT NULL,
    name_hi         VARCHAR(150) NOT NULL,
    description_en  TEXT,
    description_hi  TEXT,
    min_landholding ENUM('small','medium','large') DEFAULT NULL,
    crop_category   ENUM('foodgrain','horticulture','cash_crop') DEFAULT NULL,
    requires_loss   BOOLEAN DEFAULT FALSE,
    requires_no_soil_card BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------
-- Health history — a farmer's tracked record per plant over time
-- ---------------------------------------------------------------
CREATE TABLE health_history (
    history_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    prediction_id   BIGINT NOT NULL,
    notes           VARCHAR(255),
    recorded_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (prediction_id) REFERENCES predictions(prediction_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------
-- Seed data
-- ---------------------------------------------------------------
INSERT INTO plant_species (name_en, name_hi, category) VALUES
 ('Tomato','टमाटर','horticulture'),
 ('Potato','आलू','foodgrain'),
 ('Apple','सेब','horticulture'),
 ('Maize','मक्का','foodgrain'),
 ('Grape','अंगूर','horticulture');

INSERT INTO diseases (species_id, class_label, name_en, name_hi, description_en, description_hi, severity) VALUES
 (1,'Tomato___Early_blight','Early blight','अगेती झुलसा रोग','Dark concentric spots on older leaves, spreading upward.','पुरानी पत्तियों पर गहरे संकेंद्रित धब्बे, ऊपर की ओर फैलते हुए।','moderate'),
 (1,'Tomato___Late_blight','Late blight','पछेती झुलसा रोग','Water-soaked patches turning brown fast, can destroy crop within days.','पानी से भीगे धब्बे तेज़ी से भूरे होते हैं, दिनों में फसल नष्ट कर सकते हैं।','severe'),
 (1,'Tomato___healthy','Healthy','स्वस्थ','No visible disease symptoms detected on this leaf.','इस पत्ती पर कोई दृश्य रोग लक्षण नहीं मिला।','none'),
 (2,'Potato___Early_blight','Early blight','अगेती झुलसा रोग','Brown target-like spots on lower leaves.','निचली पत्तियों पर भूरे लक्ष्य-जैसे धब्बे।','moderate'),
 (2,'Potato___Late_blight','Late blight','पछेती झुलसा रोग','Rapid leaf collapse in humid weather.','नम मौसम में पत्तियों का तेज़ी से गिरना।','severe'),
 (2,'Potato___healthy','Healthy','स्वस्थ','No visible disease symptoms detected on this leaf.','इस पत्ती पर कोई दृश्य रोग लक्षण नहीं मिला।','none'),
 (3,'Apple___Scab','Apple scab','सेब की पपड़ी रोग','Olive-green to black scabby spots on leaves and fruit.','पत्तियों और फल पर जैतूनी-हरे से काले पपड़ीदार धब्बे।','moderate'),
 (3,'Apple___healthy','Healthy','स्वस्थ','No visible disease symptoms detected on this leaf.','इस पत्ती पर कोई दृश्य रोग लक्षण नहीं मिला।','none'),
 (4,'Corn___Common_rust','Common rust','सामान्य रतुआ रोग','Small reddish-brown pustules on both leaf surfaces.','पत्ती की दोनों सतहों पर छोटे लाल-भूरे फुंसीनुमा धब्बे।','moderate'),
 (4,'Corn___healthy','Healthy','स्वस्थ','No visible disease symptoms detected on this leaf.','इस पत्ती पर कोई दृश्य रोग लक्षण नहीं मिला।','none'),
 (5,'Grape___Black_rot','Black rot','काला सड़न रोग','Circular brown lesions with dark borders, fruit shrivels.','गहरे किनारों वाले गोलाकार भूरे घाव, फल सिकुड़ जाता है।','severe'),
 (5,'Grape___healthy','Healthy','स्वस्थ','No visible disease symptoms detected on this leaf.','इस पत्ती पर कोई दृश्य रोग लक्षण नहीं मिला।','none');

INSERT INTO care_guides (disease_id, water_en, water_hi, sunlight_en, sunlight_hi, fertilizer_en, fertilizer_hi, next_step_en, next_step_hi) VALUES
 (1,'Water at soil level, avoid wetting leaves','मिट्टी के स्तर पर पानी दें, पत्तियों को गीला करने से बचें','6+ hrs full sun','6+ घंटे पूर्ण धूप','Balanced NPK, avoid excess nitrogen','संतुलित एनपीके, अधिक नाइट्रोजन से बचें','Remove affected leaves, apply fungicide','प्रभावित पत्तियां हटाएं, फफूंदनाशक लगाएं'),
 (2,'Reduce overhead watering immediately','ऊपर से पानी देना तुरंत कम करें','Full sun, improve airflow','पूर्ण धूप, हवा का प्रवाह बढ़ाएं','Hold nitrogen until recovery','ठीक होने तक नाइट्रोजन रोकें','Apply fungicide within 24 hours, isolate plant','24 घंटे में फफूंदनाशक लगाएं, पौधे को अलग करें'),
 (3,'Regular watering, keep soil moist','नियमित पानी, मिट्टी नम रखें','6+ hrs full sun','6+ घंटे पूर्ण धूप','Standard monthly feed','मासिक मानक खाद','Continue routine monitoring','नियमित निगरानी जारी रखें');

INSERT INTO schemes (code, name_en, name_hi, description_en, description_hi, min_landholding, crop_category, requires_loss, requires_no_soil_card) VALUES
 ('PMKISAN','PM-KISAN income support','पीएम-किसान आय सहायता','Direct annual income support for landholding farmer families.','भूमि-धारक किसान परिवारों के लिए प्रत्यक्ष वार्षिक आय सहायता।','small',NULL,FALSE,FALSE),
 ('PMFBY','PMFBY crop insurance','पीएमएफबीवाई फसल बीमा','Insurance cover and claim support for crop loss from disease or weather.','रोग या मौसम से फसल हानि के लिए बीमा कवर और दावा सहायता।',NULL,NULL,TRUE,FALSE),
 ('SOILHEALTH','Soil Health Card scheme','मृदा स्वास्थ्य कार्ड योजना','Free soil testing and nutrient recommendations for the farm.','खेत के लिए मुफ़्त मिट्टी परीक्षण और पोषक तत्व सिफारिशें।',NULL,NULL,FALSE,TRUE),
 ('KCC','Kisan Credit Card','किसान क्रेडिट कार्ड','Low-interest credit for crop input costs and recovery after loss.','फसल लागत और नुकसान के बाद रिकवरी के लिए कम ब्याज दर पर ऋण।',NULL,'cash_crop',TRUE,FALSE),
 ('MIDH','Mission for Integrated Development of Horticulture','बागवानी के समग्र विकास हेतु मिशन','Support for horticulture growers, including disease management inputs.','बागवानी उत्पादकों के लिए सहायता, जिसमें रोग प्रबंधन इनपुट शामिल हैं।',NULL,'horticulture',FALSE,FALSE);
