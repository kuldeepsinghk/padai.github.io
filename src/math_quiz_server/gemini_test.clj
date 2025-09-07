(ns math-quiz-server.gemini-test
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [math-quiz-server.models :as models]
            [math-quiz-server.constants :as const]))

(def api-key
  (or (System/getenv "GEMINI_API_KEY")
      (throw (Exception. "GEMINI_API_KEY environment variable not set"))))
(def gemini-api-url "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")

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

(defn clean-json-text [text]
  (-> text
      (clojure.string/replace #"```json\s*" "")  ; Remove opening ```json
      (clojure.string/replace #"```\s*" "")      ; Remove closing ```
      (clojure.string/trim)))                    ; Remove any extra whitespace

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

(defn generate-text [topic num-questions curriculum-spec & [subject chapter-info]]
  (let [grade (:grade curriculum-spec)
        chapter-name (when chapter-info (:chapter chapter-info))
        prompt (generate-prompt topic num-questions grade subject chapter-name)]

    (println prompt)
    (try
      (let [request-body (json/encode {:contents [{:parts [{:text prompt}]}]})
            response (client/post gemini-api-url
                                  {:headers {"Content-Type" "application/json"}
                                   :query-params {:key api-key}
                                   :body request-body
                                   :as :json})
            raw-text (-> response :body :candidates first :content :parts first :text)]
        (clean-json-text raw-text))
      (catch Exception e
        (println "Error:" (.getMessage e))
        nil))))

(defn read-existing-questions [file]
  (try
    (if (.exists file)
      (do
        (println "Reading existing questions from" (.getAbsolutePath file))
        (with-open [reader (io/reader file)]
          (doall (json/parse-stream reader true))))
      (do
        (println "File does not exist yet:" (.getAbsolutePath file))
        []))
    (catch Exception e
      (println "Error reading existing questions:" (.getMessage e))
      [])))

(defn -main [& args]
  (let [grade (if (seq args)
                (Integer/parseInt (first args))
                7) ;; Default to 7th grade if not specified
        ;; Get the curriculum for the specified grade
        curriculum-spec (const/curriculum-for-grade grade)
        num-questions 2]

    (println "Generating questions for grade:" grade)

    ;; Process each subject separately
    (doseq [subject (:subjects curriculum-spec)]
      (let [topics (get (:subject-topics curriculum-spec) subject)
            subject-questions (atom [])
            subject-lower (clojure.string/lower-case subject)
            file (io/file (str "resources/public/quiz-data-" grade "th-" subject-lower ".json"))]

        (println "Processing subject:" subject)

        ;; Generate questions for all topics in this subject
        (doseq [topic topics]
          (println (str "Generating questions for topic: " topic " in subject: " subject))
          (when-let [generated-text (generate-text topic num-questions curriculum-spec subject {:chapter topic})]
            (println "Generated Text after cleaning:\n" generated-text)
            (try
              (let [questions (json/parse-string generated-text true)]
                (println "Successfully parsed JSON for" topic)
                (println "Questions:" (count questions))
                (swap! subject-questions concat questions))
              (catch Exception e
                (println "Error parsing JSON for topic" topic ":" (.getMessage e))))))

        ;; Print status for this subject
        (println "Total questions collected for" subject ":" (count @subject-questions))

        ;; Write subject-specific questions to their own file
        (when (seq @subject-questions)
          (println "Writing" (count @subject-questions) "questions for" subject "to file" (.getName file))

          ;; Read existing questions if file exists
          (let [existing-questions (read-existing-questions file)
                combined-questions (concat existing-questions @subject-questions)]

            (println "Found" (count existing-questions) "existing questions")
            (println "Total questions after merge:" (count combined-questions))

            ;; Write combined questions back to the file
            (with-open [writer (io/writer file)]
              (json/generate-stream combined-questions writer))

            (println "Done writing" (count combined-questions) "questions to" (.getAbsolutePath file))))))))
