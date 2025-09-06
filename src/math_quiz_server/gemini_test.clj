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

(defn clean-json-text [text]
  (-> text
      (clojure.string/replace #"```json\s*" "")  ; Remove opening ```json
      (clojure.string/replace #"```\s*" "")      ; Remove closing ```
      (clojure.string/trim)))                    ; Remove any extra whitespace

(defn generate-text [topic num-questions curriculum-spec]
  (let [grade (:grade curriculum-spec)
        prompt (str "You are an experienced CBSE Class " grade " " topic " teacher. Your task is to generate practice questions for students studying the NCERT syllabus. "
                    "Generate " num-questions " multiple-choice questions from NCERT sllyabus about " topic " for " grade "-grade students in JSON format. "
                    "Each question should have a 'category', 'question', 'options' (an array of four strings), 'correct' (the index of the correct option), and 'rationale'. "
                    "The JSON should be an array of question objects, like this: "
                    "[{\"category\": \"" topic "\", \"question\": \"question text\", \"options\": [\"option1\", \"option2\", \"option3\", \"option4\"], \"correct\": 0, \"rationale\": \"explanation\"}]")]
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
          (when-let [generated-text (generate-text topic num-questions curriculum-spec)]
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
