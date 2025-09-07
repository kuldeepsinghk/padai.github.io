(ns math-quiz-server.gemini-test-test
  (:require [clojure.test :refer :all]
            [math-quiz-server.gemini-test :as gemini]
            [math-quiz-server.models :as models]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [cheshire.core :as json]
            ))

;; Load the schema file for validation
(def schema-file-path "resources/question_schema.json")
(def question-schema 
  (try
    (json/parse-string (slurp schema-file-path) true)
    (catch Exception e
      (println "Error loading schema:" (.getMessage e))
      nil)))

;; Helper function to extract JSON examples from a string
(defn extract-json-examples [text]
  (let [matches (re-find #"\"examples\":\s*(\[[\s\S]*?\])" text)]
    (when matches
      (second matches))))

;; Define test fixture data
(def test-curriculum-spec
  (models/create-curriculum-spec
    6                                  ;; Grade
    ["Science"]                        ;; Subjects
    {"Science"                         ;; Subject topics by chapter
     {"Exploring Magnets"  ;; Chapter name
      {"key_topics" ["Classification of organisms"
                     "Plants and animals"
                     "Adaptation"
                     "Habitats"]}}}))

;; Test for the generate-text function
(deftest test-generate-text
  (testing "Prompt Output should adhere to the schema"
    (let [topic "Classification of organisms"
          num-questions 1
          grade 6
          subject "Science"
          chapter "Exploring Magnets"
          prompt (gemini/generate-text topic num-questions grade subject chapter)]

      (println "done")
      )))
