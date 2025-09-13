(ns math-quiz-server.question-gen
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [malli.core :as m]))

;; Define schema for grade-subjects.json structure
(def topic-schema 
  [:map
   [:key_topics [:vector :string]]])

;; Define schema for Grade validation
(def grade-schema 
  [:enum :grade-6 :grade-7 :grade-10])

;; Define schema for Subject validation
(def subject-schema
  [:enum :Math :Science :Physics :Chemistry :Biology])

(def grade-subjects-schema
  [:map-of 
   grade-schema
   [:map-of 
    subject-schema             ; Now using subject-schema for validation
    [:map-of
     :keyword                  ; Topic (e.g., :Fractions)
     topic-schema]]])          ; Topic details with key_topics

(defn load-validated-curriculum-data
  "Loads curriculum data from JSON file and validates it against schema.
   Returns a map with :data, :error, and :resource-path keys.
   - :data contains the parsed data if valid, nil otherwise
   - :error contains error details if validation fails, nil otherwise
   - :resource-path contains the path to the JSON file or error message"
  []
  (let [resource (io/resource "public/grade-subjects.json")
        resource-path (if resource (.getPath resource) "Resource not found")]
    (if resource
      (let [content (slurp resource)
            parsed-data (json/parse-string content true)
            valid? (m/validate grade-subjects-schema parsed-data)
            explain-result (when-not valid? (m/explain grade-subjects-schema parsed-data))]
        (if valid?
          {:data parsed-data :error nil :resource-path resource-path}
          {:data nil :error explain-result :resource-path resource-path}))
      {:data nil :error "Resource not found" :resource-path resource-path})))

(defn get-subjects-for-grade
  "Returns a list of subject keywords for the specified grade.
   This function accepts a grade keyword directly (e.g., :grade-6)."
  [grade-key]
  ;; Validate that the grade-key is valid
  (when-not (m/validate grade-schema grade-key)
    (throw (IllegalArgumentException. 
           (str "Invalid grade: " grade-key 
                ". Must be one of: " (clojure.string/join ", " (m/entries grade-schema))))))
  
  (let [{:keys [data error resource-path]} (load-validated-curriculum-data)]
    (println "Resource path:" resource-path)
    (if data
      (do
        (println "JSON data is valid according to schema")
        ;; Get subjects for the specified grade as plain keywords
        (when-let [grade-data (get data grade-key)]
          (vec (keys grade-data))))
      (do
        (println "Warning: JSON data does not match schema")
        (when error (println "Validation error:" error))
        []))))

(defn get-topics-for-subject
  "Returns a list of topics for the specified grade and subject.
   This function accepts grade and subject keywords directly (e.g., :grade-6, :Math)."
  [grade-key subject-key]
  ;; Validate that the grade-key is valid
  (when-not (m/validate grade-schema grade-key)
    (throw (IllegalArgumentException. 
           (str "Invalid grade: " grade-key 
                ". Must be one of: " (clojure.string/join ", " (m/entries grade-schema))))))
  
  ;; Validate that the subject-key is valid
  (when-not (m/validate subject-schema subject-key)
    (throw (IllegalArgumentException. 
           (str "Invalid subject: " subject-key 
                ". Must be one of: " (clojure.string/join ", " (m/entries subject-schema))))))
  
  (println "Looking up topics for subject" subject-key "in grade" grade-key)
  
  (let [{:keys [data error resource-path]} (load-validated-curriculum-data)]
    (if data
      (let [grade-data (get data grade-key)
            subject-data (get grade-data subject-key)]
        (if subject-data
          ;; Return topic names as keywords
          (vec (keys subject-data)) 
          ;; Subject not found in this grade
          (do 
            (println "Subject" subject-key "not found in grade" grade-key)
            [])))
      ;; Invalid JSON structure or resource not found
      (do
        (println "Warning: Error loading JSON data")
        (when error (println "Error details:" error))
        []))))
