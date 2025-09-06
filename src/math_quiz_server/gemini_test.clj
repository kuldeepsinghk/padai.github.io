(ns math-quiz-server.gemini-test
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [math-quiz-server.models :as models]
            [math-quiz-server.constants :as const]))

(def api-key "AIzaSyAA_FmmPaX6L86Oo1T7h6ydyfNEXFQYYU4") ; Replace with your actual API key
(def gemini-api-url "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")

(defn clean-json-text [text]
  (-> text
      (clojure.string/replace #"```json\s*" "")  ; Remove opening ```json
      (clojure.string/replace #"```\s*" "")      ; Remove closing ```
      (clojure.string/trim)))                    ; Remove any extra whitespace

(defn generate-text [topic num-questions curriculum-spec]
  (let [grade (:grade curriculum-spec)
        prompt (str "Generate " num-questions " multiple-choice questions about " topic " for " grade "-grade students in JSON format. "
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

(defn -main [& args]
  ;; Get the grade from command line args or use a default
  (let [grade (if (seq args)
                (Integer/parseInt (first args))
                7) ;; Default to 7th grade if not specified
        ;; Get the curriculum for the specified grade
        curriculum-spec (const/curriculum-for-grade grade)
        num-questions 5]
    
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
          (with-open [writer (io/writer file)]
            (json/generate-stream @subject-questions writer))
          (println "Done writing" subject "questions to" (.getAbsolutePath file)))))))
