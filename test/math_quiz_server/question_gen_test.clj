(ns math-quiz-server.question-gen-test
  (:require [clojure.test :refer :all]
            [math-quiz-server.question-gen :as gen]
            [clojure.set :as set]
            [math-quiz-server.test-helper :as test-helper]))

;; Use the Malli instrumentation fixture for all tests in this namespace
(use-fixtures :once test-helper/instrument-fixture)

(deftest test-get-subjects-for-grade
  (testing "Getting subjects using grade keywords directly"
    (let [subjects (gen/get-subjects-for-grade :grade-6)]
      (is (not (nil? subjects)) "Subjects should not be nil")
      (is (vector? subjects) "Subjects should be a vector")
      (is (pos? (count subjects)) "Should have at least one subject")
      (is (some #{:Math} subjects) "Should include Math subject")
      (is (some #{:Science} subjects) "Should include Science subject"))

    (let [subjects (gen/get-subjects-for-grade :grade-10)]
      (is (not (nil? subjects)) "Subjects should not be nil")
      (is (vector? subjects) "Subjects should be a vector")
      (is (pos? (count subjects)) "Should have at least one subject")
      (is (some #{:Math} subjects) "Should include Math subject")
      (is (some #{:Physics} subjects) "Should include Physics subject")
      (is (some #{:Chemistry} subjects) "Should include Chemistry subject")
      (is (some #{:Biology} subjects) "Should include Biology subject"))
    ))

(deftest test-get-topics-for-subject
  (testing "Getting topics for a specific subject in a grade"
    (println "\n--- BEGIN TEST DEBUG INFO ---")
    (println "Running test for Math topics in Grade 6")
    
    (let [topics (gen/get-topics-for-subject :grade-6 :Math)]
      (println "Topics returned:" topics)
      (println "--- END TEST DEBUG INFO ---\n")
      
      (is (not (nil? topics)) "Topics should not be nil")
      (is (vector? topics) "Topics should be a vector")
      (is (< 3 (count topics)) "Topic count should be more than 3")
      (is (set/subset? #{:Fractions :Geometry :Measurement} (set topics))
          "Should include Fractions, Geometry, and Measurement topics")
      )))
