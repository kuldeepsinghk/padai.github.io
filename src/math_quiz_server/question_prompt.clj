(ns math-quiz-server.question-prompt
  (:require [cheshire.core :as json]))

(def schema-file-path "resources/question_schema.json")

(defn read-schema-file []
  "Reads the question schema JSON file"
  (try
    (let [schema-str (slurp schema-file-path)]
      (json/parse-string schema-str true))
    (catch Exception e
      (println "Error reading schema file:" (.getMessage e))
      nil)))

(def question-schema (read-schema-file))

(defn generate-prompt
  ([topic num-questions grade]
   (generate-prompt topic num-questions grade nil nil))

  ([topic num-questions grade subject chapter-name]
   (let [subject-context (if subject
                           (str ", " subject)
                           "")
         chapter-context (if chapter-name
                           (str "This topic is part of the chapter \"" chapter-name "\". ")
                           "")
         schema-json (json/generate-string question-schema {:pretty true})
         ;; Update schema example categories to match the current topic
         schema-with-topic (clojure.string/replace schema-json #"\"category\": \"[^\"]+\"" (str "\"category\": \"" topic "\""))]
     (str "You are an experienced CBSE Class " grade subject-context " teacher. "
          "Your task is to generate practice questions for students studying the NCERT syllabus. "
          "Generate " num-questions " multiple-choice questions from NCERT sllyabus about " topic " for " grade "-grade students in JSON format. "
          chapter-context
          "The output MUST strictly conform to this JSON Schema:\n\n"
          schema-with-topic
          "\n\nFollow this schema exactly without deviating. Make sure to include exactly " num-questions " questions."))))
