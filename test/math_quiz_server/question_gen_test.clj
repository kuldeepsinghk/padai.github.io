(ns math-quiz-server.question-gen-test
  (:require [clojure.test :refer :all]
            [math-quiz-server.question-gen :as gen]))

(deftest test-grade-constructor
  (testing "Grade constructor creates valid instances"
    (is (instance? math_quiz_server.question_gen.GradeType (gen/grade :grade-7)) 
        "Should create a GradeType instance for valid grade")
    (is (= :grade-7 (:value (gen/grade :grade-7)))
        "Created grade should contain the correct value"))
      
  (testing "Grade constructor rejects invalid values"
    (is (thrown? IllegalArgumentException 
         (gen/grade :grade-13)) 
        "Should reject invalid grade numbers")
    (is (thrown? IllegalArgumentException 
         (gen/grade :invalid)) 
        "Should reject non-grade keywords")))

(deftest test-get-subjects-for-grade
  (testing "Getting subjects using type-safe Grade instance"
    (println "\n--- BEGIN TEST DEBUG INFO ---")
    (println "Running test with type-safe grade instance: gen/grade-7")
    
    (let [subjects (gen/get-subjects-for-grade gen/grade-7)]
      (println "Subjects returned:" subjects)
      (println "--- END TEST DEBUG INFO ---\n")
      
      (is (not (nil? subjects)) "Subjects should not be nil")
      (is (vector? subjects) "Subjects should be a vector")
      (is (pos? (count subjects)) "Should have at least one subject"))))
