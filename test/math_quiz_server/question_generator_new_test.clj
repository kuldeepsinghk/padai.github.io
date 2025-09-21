(ns math-quiz-server.question-generator-new-test
  (:require [clojure.test :refer :all]
            [math-quiz-server.question-generator-new :as gen]
            [math-quiz-server.question-prompt-new :as prompt]
            [math-quiz-server.question-gen :as curr]
            [clojure.string :as string]
            [clojure.java.io :as io]))

;; Test generate-questions function
(deftest test-generate-questions
  (testing "Generate questions with actual API call"
    (let [grade :grade-6
          subject :Math
          topic :Fractions
          num-questions 5
          result (gen/generate-questions grade subject topic num-questions)]

      (println result)
      
      ;; Test that result is not nil
      (is (not (nil? result)) "Result should not be nil")
      
      ;; Test that result is a vector
      (is (vector? result) "Result should be a vector")
      
      ;; Test that result contains questions with expected structure
      (when (seq result)
        (is (contains? (first result) :category) "Questions should have category field")))))

;; Test generate-questions-by-curriculum function
(deftest test-generate-questions-by-curriculum-no-save
  (testing "Generate questions without saving to file"
    (let [grade :grade-7
          subject :Science
          save-to-file? false
          result (gen/generate-questions-by-curriculum grade subject save-to-file?)]
      
      (println "Generated questions:" (count result))
      
      ;; Test that result is not nil
      (is (not (nil? result)) "Result should not be nil")
      
      ;; Test that result is a vector (not a map with :questions key)
      (is (vector? result) "Result should be a vector when not saving to file")
      
      ;; Test that questions have proper structure
      (when (seq result)
        (is (contains? (first result) :category) "Questions should have category field")))))

;; Test generate-questions-by-curriculum function
(deftest test-generate-questions-by-curriculum
  (testing "Generate questions for all topics in curriculum and save to file"
    (let [grade :grade-7
          subject :Math
          save-to-file? false
          result (gen/generate-questions-by-curriculum grade subject save-to-file?)]

      (println "Generated questions:" (count (:questions result)))
      (println "Saved to file:" (:file-path result))
      
      ;; Test that result is not nil
      (is (not (nil? result)) "Result should not be nil")
      
      ;; Test that result contains questions
      (is (vector? (:questions result)) "Questions should be a vector")
      
      ;; Test that file path is returned
      (is (string? (:file-path result)) "File path should be a string")
      
      ;; Test that questions have proper structure
      (when (seq (:questions result))
        (is (contains? (first (:questions result)) :category) "Questions should have category field")))))

(deftest ^:skip test-generate-questions-for-all-subjects
  (testing "Generate questions for all subjects in a grade"
    (let [grade :grade-6
          subjects (curr/get-curriculum-items grade)]
      
      (is (seq subjects) (str "No subjects found for grade " (name grade)))
      (println (str "\nFound " (count subjects) " subjects for grade " (name grade) ": " 
                  (string/join ", " (map name subjects))))
      
      (doseq [subject subjects]
        (testing (str "Generating questions for " (name subject))
          (let [result (gen/generate-questions-by-curriculum grade subject true 20)]
            (println (str "✅ Generated " (count result) " questions for " (name subject)))
            ))))))
