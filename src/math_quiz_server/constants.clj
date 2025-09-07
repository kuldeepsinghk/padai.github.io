(ns math-quiz-server.constants
  (:require [math-quiz-server.models :refer [create-curriculum-spec]]
            [cheshire.core :as json]
            [clojure.java.io :as io]))

;; Load curriculum data from JSON file
(defn load-curriculum-data []
  (try
    (let [file (io/resource "public/grade-subjects.json")
          content (slurp file)]
      (json/parse-string content false)) 
    (catch Exception e
      (println "Error loading grade-subjects.json:" (.getMessage e)))))

;; Convert grade strings like "6th" to integers like 6
(defn grade-str-to-int [grade-str]
  (let [grade-str-clean (if (keyword? grade-str)
                          (name grade-str)
                          (str grade-str))]
    (Integer/parseInt (clojure.string/replace grade-str-clean #"th$" ""))))

;; Convert the JSON format to the format needed for topics-by-grade-and-subject
(defn convert-json-to-topics-format [json-data]
  (reduce (fn [acc [grade-str subjects-map]]
            (let [grade-int (grade-str-to-int grade-str)
                  subjects-with-topics (reduce (fn [subj-acc [subject chapters-map]]
                                                (assoc subj-acc 
                                                       subject 
                                                       (mapcat (fn [[_ chapter-data]]
                                                                (get chapter-data "key_topics"))
                                                              chapters-map)))
                                              {}
                                              subjects-map)]
              (assoc acc grade-int subjects-with-topics)))
          {}
          json-data))

;; Load the curriculum data and convert to our internal format
(def curriculum-data (load-curriculum-data))
(def topics-by-grade-and-subject (convert-json-to-topics-format curriculum-data))

;; Function to create a curriculum spec for a specific grade
(defn get-grade-curriculum [grade]
  (let [subjects (keys (get topics-by-grade-and-subject grade))
        subject-topics (get topics-by-grade-and-subject grade)]
    (create-curriculum-spec grade subjects subject-topics)))

;; Pre-defined curriculum specs
(def grade-6-curriculum (get-grade-curriculum 6))
(def grade-7-curriculum (get-grade-curriculum 7))
(def grade-10-curriculum (get-grade-curriculum 10))

;; Function to get curriculum for a specific grade
(defn curriculum-for-grade [grade]
  (case grade
    6 grade-6-curriculum
    7 grade-7-curriculum
    10 grade-10-curriculum
    grade-7-curriculum))
