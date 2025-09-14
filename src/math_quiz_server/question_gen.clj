(ns math-quiz-server.question-gen
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [malli.core :as m]
            [malli.instrument :as mi]))

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


(m/=> load-validated-curriculum-data
  [:=> [:cat]   [:map
                 [:data {:optional true} [:maybe [:map]]]
                 [:error {:optional true} [:maybe [:any]]]
                 [:resource-path :string]]])

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

(m/=> get-curriculum-items
  [:function
   [:=> [:cat grade-schema] [:vector subject-schema]]
   [:=> [:cat grade-schema subject-schema] [:vector :keyword]]])

(defn get-curriculum-items
  "Unified function to retrieve curriculum items by navigation path.
   With one argument, returns subjects for a grade.
   With two arguments, returns topics for a subject within a grade."
  
  ;; First arity - Get subjects for a grade
  ([grade-key]
   (let [{:keys [data error]} (load-validated-curriculum-data)]
     (if data
       (some-> data grade-key keys vec)
       (do
         (println "Warning: Error loading curriculum data")
         (when error (println "Details:" error))
         []))))
  
  ;; Second arity - Get topics for a subject within a grade
  ([grade-key subject-key]
   (println "Looking up" subject-key "in" grade-key)
   (let [{:keys [data error]} (load-validated-curriculum-data)]
     (if data
       (if-let [grade-data (grade-key data)]
         (if-let [subject-data (subject-key grade-data)]
           (vec (keys subject-data))
           (do
             (println "Subject" subject-key "not found in grade" grade-key)
             []))
         [])
       (do
         (println "Warning: Error loading curriculum data")
         (when error (println "Details:" error))
         [])))))

;; Instrumentation utilities for development and testing

(defn instrument-all!
  "Enable runtime validation for all functions with Malli type annotations.
   Call this during development and testing to catch invalid inputs/outputs."
  []
  (mi/instrument!))

(defn unstrument-all!
  "Disable runtime validation for all functions.
   Call this in production for better performance."
  []
  (mi/unstrument!))

;; Comment out to enable validation in REPL sessions
;; (instrument-all!)
