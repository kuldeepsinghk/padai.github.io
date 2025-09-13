(ns math-quiz-server.question-gen
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [malli.core :as m]))

;; Define Grade as an enum with keyword values
(def Grade
  {:keys #{:grade-6 :grade-7 :grade-10}
   :valid? (fn [value] (contains? #{:grade-6 :grade-7 :grade-10} value))
   :to-string (fn [value] 
                (when ((:valid? Grade) value)
                  (str (subs (name value) 6) "th")))})

;; Define schema for grade-subjects.json structure
(def topic-schema 
  [:map
   [:key_topics [:vector :string]]])

;; Define schema for Grade validation
(def grade-schema 
  [:enum :grade-6 :grade-7 :grade-10])

(def grade-subjects-schema
  [:map-of 
   :keyword                     ; Grade (e.g., :7th) - Changed from :string
   [:map-of 
    :keyword                    ; Subject (e.g., :Math) - Changed from :string
    [:map-of
     :keyword                   ; Topic (e.g., :Fractions) - Changed from :string
     topic-schema]]])           ; Topic details with key_topics

(defn get-subjects-for-grade
  "Given a grade keyword (must be one of :grade-6, :grade-7, or :grade-10),
   returns a list of subjects for that grade."
  [grade-key]
  ;; Use Malli schema to validate the input
  (if (m/validate grade-schema grade-key)
    (let [grade-str ((:to-string Grade) grade-key)
          resource (io/resource "public/grade-subjects.json")]
      (println "Resource path:" (if resource (.getPath resource) "Resource not found"))
      (if resource
        (let [content (slurp resource)
              parsed-data (json/parse-string content true)]
          ;; Validate with detailed explanation
          (let [valid? (m/validate grade-subjects-schema parsed-data)
                explain-result (when-not valid? (m/explain grade-subjects-schema parsed-data))]
            (if valid?
              (do
                (println "JSON data is valid according to schema")
                ;; Get subjects for the specified grade
                (when-let [grade-data (get parsed-data (keyword ((:to-string Grade) grade-key)))]
                  (vec (keys grade-data))))
              (do
                (println "Warning: JSON data does not match schema")
                (println "Validation error:" explain-result)
                []))))
        nil))
    (let [explain-result (m/explain grade-schema grade-key)]
      (throw (IllegalArgumentException. 
               (str "Invalid grade: " grade-key 
                    ". Must be one of: " (clojure.string/join ", " (:keys Grade))
                    "\nValidation error: " explain-result))))))
