(ns math-quiz-server.question-gen-test
  (:require [clojure.test :refer :all]
            [math-quiz-server.question-gen :as gen])
 )

(deftest test-get-subjects-for-grade
  (testing "Getting subjects using grade keywords directly"
    (let [subjects (gen/get-subjects-for-grade :grade-7)]
      (is (not (nil? subjects)) "Subjects should not be nil")
      (is (vector? subjects) "Subjects should be a vector")
      (is (pos? (count subjects)) "Should have at least one subject")
      (is (some #{gen/subject-Math} subjects) "Should include Math subject")
      (is (some #{gen/subject-Science} subjects) "Should include Science subject"))

    (let [subjects (gen/get-subjects-for-grade :grade-10)]
      (is (not (nil? subjects)) "Subjects should not be nil")
      (is (vector? subjects) "Subjects should be a vector")
      (is (pos? (count subjects)) "Should have at least one subject")
      (is (some #{gen/subject-Math} subjects) "Should include Math subject")
      (is (some #{gen/subject-Physics} subjects) "Should include Physics subject")
      (is (some #{gen/subject-Chemistry} subjects) "Should include Chemistry subject")
      (is (some #{gen/subject-Biology} subjects) "Should include Biology subject"))
    ))

(deftest test-get-topics-for-subject
  (testing "Getting topics for a specific subject in a grade"
    (println "\n--- BEGIN TEST DEBUG INFO ---")
    (println "Running test for Math topics in Grade 6")
    
    (let [topics (gen/get-topics-for-subject :grade-6 gen/subject-Math)]
      (println "Topics returned:" topics)
      (println "--- END TEST DEBUG INFO ---\n")
      
      (is (not (nil? topics)) "Topics should not be nil")
      (is (vector? topics) "Topics should be a vector")
      (is (pos? (count topics)) "Should have at least one topic")
      (is (some #(= (name %) "Basic Arithmetic") (map name topics)) "Should include Basic Arithmetic as a topic"))))
