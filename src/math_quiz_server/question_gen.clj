(ns math-quiz-server.question-gen
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [malli.core :as m]))

;; Define schema for grade-subjects.json structure
(def topic-schema 
  [:map
   [:key_topics [:vector :string]]])

(def grade-subjects-schema
  [:map-of 
   :keyword                     ; Grade (e.g., :7th) - Changed from :string
   [:map-of 
    :keyword                    ; Subject (e.g., :Math) - Changed from :string
    [:map-of
     :keyword                   ; Topic (e.g., :Fractions) - Changed from :string
     topic-schema]]])           ; Topic details with key_topics

(defn git get-subjects-for-grade
  "Given a grade (e.g., '6', '7', '10'), returns a list of subjects for that grade."
  [class]
  (let [grade-str (str class "th")
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
              (when-let [grade-data (get parsed-data (keyword grade-str))]
                (vec (keys grade-data))))
            (do
              (println "Warning: JSON data does not match schema")
              (println "Validation error:" explain-result)
              []))))
      nil)))
