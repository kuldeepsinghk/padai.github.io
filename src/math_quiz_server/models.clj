(ns math-quiz-server.models)

;; Define the CurriculumSpec record type
(defrecord CurriculumSpec [grade subjects subject-topics])

(defn create-curriculum-spec [grade subjects subject-topics]
  (->CurriculumSpec grade subjects subject-topics))

;; Export for other namespaces
(def curriculum-spec-type (type (->CurriculumSpec 0 [] {})))
