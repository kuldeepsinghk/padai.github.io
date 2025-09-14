(ns math-quiz-server.question-generator-new-test
  (:require [clojure.test :refer :all]
            [math-quiz-server.question-generator-new :as gen]
            [math-quiz-server.question-prompt-new :as prompt]
            [math-quiz-server.question-gen :as curr]
            [clojure.string :as string]))

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
(deftest test-generate-questions-by-curriculum
  (testing "Generate questions for all topics in curriculum"
    (let [grade :grade-6
          subject :Math
          result (gen/generate-questions-by-curriculum grade subject)]

      (println result)
      
      ;; Test that result is not nil
      (is (not (nil? result)) "Result should not be nil")
      
      ;; Test that result is a vector
      (is (vector? result) "Result should be a vector")
      
      ;; The result should contain questions from multiple topics
      (when (seq result)
        (is (contains? (first result) :category) "Questions should have category field")))))
