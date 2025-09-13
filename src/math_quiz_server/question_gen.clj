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

;; Define a record type for Grade
(defrecord GradeType [value])

;; Smart constructor that ensures only valid Grade values can be created
(defn grade
  "Creates a Grade instance with validation. Throws IllegalArgumentException if invalid."
  [key-value]
  (if ((:valid? Grade) key-value)
    (->GradeType key-value)
    (throw (IllegalArgumentException. 
             (str "Invalid grade: " key-value 
                  ". Must be one of: " (clojure.string/join ", " (:keys Grade)))))))

;; Convenience helper constants for common grades
(def grade-6 (grade :grade-6))
(def grade-7 (grade :grade-7))
(def grade-10 (grade :grade-10))

;; Define Subject as an enum with keyword values
(def Subject
  {:keys #{:subject-Math :subject-Science :subject-Physics :subject-Chemistry :subject-Biology}
   :valid? (fn [value] (contains? #{:subject-Math :subject-Science :subject-Physics 
                                   :subject-Chemistry :subject-Biology} value))
   :to-string (fn [value] 
                (when ((:valid? Subject) value)
                  (subs (name value) 8)))})

;; Define a record type for Subject
(defrecord SubjectType [value])

;; Smart constructor that ensures only valid Subject values can be created
(defn subject
  "Creates a Subject instance with validation. Throws IllegalArgumentException if invalid."
  [key-value]
  (if ((:valid? Subject) key-value)
    (->SubjectType key-value)
    (throw (IllegalArgumentException. 
             (str "Invalid subject: " key-value 
                  ". Must be one of: " (clojure.string/join ", " (:keys Subject)))))))

;; Convenience helper constants for common subjects
(def subject-Math (subject :subject-Math))
(def subject-Science (subject :subject-Science)) 
(def subject-Physics (subject :subject-Physics))
(def subject-Chemistry (subject :subject-Chemistry))
(def subject-Biology (subject :subject-Biology))

;; Define schema for grade-subjects.json structure
(def topic-schema 
  [:map
   [:key_topics [:vector :string]]])

;; Define schema for Grade validation
(def grade-schema 
  [:enum :grade-6 :grade-7 :grade-10])

;; Define schema for Subject validation
(def subject-schema
  [:enum :subject-Math :subject-Science :subject-Physics :subject-Chemistry :subject-Biology])

(def grade-subjects-schema
  [:map-of 
   :keyword                     ; Grade (e.g., :7th) - Changed from :string
   [:map-of 
    :keyword                    ; Subject (e.g., :Math) - Changed from :string
    [:map-of
     :keyword                   ; Topic (e.g., :Fractions) - Changed from :string
     topic-schema]]])           ; Topic details with key_topics

(defn keyword->subject
  "Converts a simple subject keyword (e.g., :Math) to a SubjectType instance"
  [kw]
  (subject (keyword (str "subject-" (name kw)))))

(defn get-subjects-for-grade
  "Given a Grade instance, returns a list of SubjectType instances for that grade.
   This function requires a proper Grade instance created with the grade function."
  [^GradeType grade-instance]
  (let [grade-key (:value grade-instance)
        grade-str ((:to-string Grade) grade-key)
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
              ;; Get subjects for the specified grade and convert to SubjectType instances
              (when-let [grade-data (get parsed-data (keyword ((:to-string Grade) grade-key)))]
                (mapv keyword->subject (keys grade-data))))
            (do
              (println "Warning: JSON data does not match schema")
              (println "Validation error:" explain-result)
              []))))
      nil)))

(defn get-topics-for-subject
  "Given a Grade instance and a Subject instance, returns a list of topics for that subject.
   This function requires proper Grade and Subject instances created with their respective constructors."
  [^GradeType grade-instance ^SubjectType subject-instance]
  (let [grade-key (:value grade-instance)
        subject-key (:value subject-instance)
        grade-str ((:to-string Grade) grade-key)
        subject-str ((:to-string Subject) subject-key)
        resource (io/resource "public/grade-subjects.json")]
    (println "Looking up topics for subject" subject-str "in grade" grade-str)
    (if resource
      (let [content (slurp resource)
            parsed-data (json/parse-string content true)]
        ;; Validate data structure
        (if (m/validate grade-subjects-schema parsed-data)
          (let [grade-data (get parsed-data (keyword grade-str))
                subject-data (get grade-data (keyword subject-str))]
            (if subject-data
              ;; Return topic names as keywords
              (vec (keys subject-data)) 
              ;; Subject not found in this grade
              (do 
                (println "Subject" subject-str "not found in grade" grade-str)
                [])))
          ;; Invalid JSON structure
          (do
            (println "Warning: JSON data does not match schema")
            [])))
      ;; Resource not found
      (do
        (println "Resource not found")
        nil))))
