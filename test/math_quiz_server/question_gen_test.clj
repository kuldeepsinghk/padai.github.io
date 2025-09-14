(ns math-quiz-server.question-gen-test
  (:require [clojure.test :refer :all]
            [math-quiz-server.question-gen :as gen]
            [clojure.set :as set]
            [math-quiz-server.test-helper :as test-helper]))

;; Use the Malli instrumentation fixture for all tests in this namespace
(use-fixtures :once test-helper/instrument-fixture)

(deftest test-get-curriculum-items
  (testing "Unified API function for subjects"
    (let [subjects (gen/get-curriculum-items :grade-7)]
      (is (not (nil? subjects)) "Subjects should not be nil")
      (is (vector? subjects) "Subjects should be a vector")
      (is (pos? (count subjects)) "Should have at least one subject")
      (is (some #{:Math} subjects) "Should include Math subject")
      (is (some #{:Science} subjects) "Should include Science subject")))
  
  (testing "Unified API function for topics"
    (let [topics (gen/get-curriculum-items :grade-6 :Math)]
      (is (not (nil? topics)) "Topics should not be nil")
      (is (vector? topics) "Topics should be a vector")
      (is (< 3 (count topics)) "Topic count should be more than 3")
      (is (set/subset? #{:Fractions :Geometry :Measurement} (set topics))
          "Should include Fractions, Geometry, and Measurement topics"))))
