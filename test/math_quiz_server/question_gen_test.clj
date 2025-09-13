(ns math-quiz-server.question-gen-test
  (:require [clojure.test :refer :all]
            [math-quiz-server.question-gen :as gen]))

(deftest test-get-subjects-for-grade
  (testing "Getting subjects for valid grades"
    ;; Add print statements to see more debugging info
    (println "\n--- BEGIN TEST DEBUG INFO ---")
    (println "Running test with grade 7")
    
    (let [subjects (gen/get-subjects-for-grade 7)]
      (println "Subjects returned:" subjects)
      (println "--- END TEST DEBUG INFO ---\n")
      
      (is (not (nil? subjects)) "Subjects should not be nil")
      (is (vector? subjects) "Subjects should be a vector")
      (is (pos? (count subjects)) "Should have at least one subject"))))



