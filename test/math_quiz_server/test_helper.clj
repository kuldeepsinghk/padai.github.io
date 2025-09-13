(ns math-quiz-server.test-helper
  (:require [clojure.test :refer :all]
            [math-quiz-server.question-gen :as qgen]))

(defn instrument-fixture
  "Test fixture that enables Malli instrumentation during tests.
   This ensures all function calls are validated against their schema definitions."
  [f]
  (println "Enabling Malli instrumentation for tests...")
  (qgen/instrument-all!)
  (try
    (f)  ; Run the test
    (finally
      (println "Disabling Malli instrumentation...")
      (qgen/unstrument-all!))))

;; Usage in test files:
;; (use-fixtures :once test-helper/instrument-fixture)
