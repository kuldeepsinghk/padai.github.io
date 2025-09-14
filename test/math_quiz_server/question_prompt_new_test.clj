(ns math-quiz-server.question-prompt-new-test
  (:require [clojure.test :refer :all]
            [math-quiz-server.question-prompt-new :as prompt]))

(deftest test-create-gemini-prompt-basic-structure
  (testing "Prompt contains the required basic elements"
    (let [grade :grade-6
          subject :Math
          topic :Fractions
          num-questions 3
          prompt-text (prompt/create-gemini-prompt grade subject topic num-questions)]

      (println prompt-text)
      
      ;; Test basic structure requirements
      (is (string? prompt-text) "Prompt should be a string")
      (is (.contains prompt-text "Generate 3") "Should specify number of questions")
      (is (.contains prompt-text "grade 6") "Should mention the grade")
      (is (.contains prompt-text "Math") "Should mention the subject")
      (is (.contains prompt-text "Fractions") "Should mention the topic"))))

(deftest test-create-gemini-prompt-json-structure
  (testing "Prompt contains required JSON structure and details"
    (let [grade :grade-6
          subject :Math
          topic :Fractions
          num-questions 3
          prompt-text (prompt/create-gemini-prompt grade subject topic num-questions)]
      
      ;; Test JSON structure requirements - look for field names without requiring specific formatting
      (is (re-find #"category" prompt-text) "Should include category field")
      (is (re-find #"question" prompt-text) "Should include question field")
      (is (re-find #"options" prompt-text) "Should include options field")
      (is (re-find #"correct" prompt-text) "Should include correct field")
      (is (re-find #"rationale" prompt-text) "Should include rationale field"))))

(deftest test-validate-llm-response
  (testing "Validation of valid LLM response"
    (let [valid-response "[{\"category\":\"Fractions\",\"question\":\"A\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"correct\":2,\"rationale\":\"A\"},{\"category1\":\"B\",\"question\":\"B\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"correct\":2,\"rationale\":\"\"}]"
          validation-result (prompt/validate-llm-response valid-response)]
      
      (is (not (nil? validation-result)) "Valid response should not return nil")
      (is (vector? validation-result) "Result should be a vector")
      (is (= 1 (count validation-result)) "Should have two question")
      (is (= "Fractions" (-> validation-result first :category)) "Category should match")
      )))
