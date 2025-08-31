(ns math-quiz-server.gemini-test
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.java.io :as io]))

(def api-key "AIzaSyCH_YCEP4Lm9CA4sFb6c1eAmXLlkJWHCtY") ; Replace with your actual API key
(def gemini-api-url "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")



(defn clean-json-text [text]
  (-> text
      (clojure.string/replace #"```json\s*" "")  ; Remove opening ```json
      (clojure.string/replace #"```\s*" "")      ; Remove closing ```
      (clojure.string/trim)))                    ; Remove any extra whitespace

(defn generate-text [topic num-questions]
  (let [prompt (str "Generate " num-questions " multiple-choice questions about " topic " for 6th-grade students in JSON format. "
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


(defn -main []
  (let [topics ["Fractions" "LCM and HCF" "Whole Numbers" "Integers" "Number Line"]
        num-questions 5
        file (io/file "resources/public/quiz-data-1.json")
        all-questions (atom [])]

    ;; First collect all questions
    (doseq [topic topics]
      (println (str "Generating questions for topic: " topic))
      (when-let [generated-text (generate-text topic num-questions)]
        (println "Generated Text after cleaning:\n" generated-text)
        (try
          (let [questions (json/parse-string generated-text true)]
            (println "Successfully parsed JSON for" topic)
            (println "Questions:" (count questions))
            (swap! all-questions concat questions))
          (catch Exception e
            (println "Error parsing JSON for topic" topic ":" (.getMessage e))))))

    ;; Print final state before writing
    (println "Total questions collected:" (count @all-questions))

    ;; Write only if we have questions
    (when (seq @all-questions)
      (println "Writing" (count @all-questions) "questions to file")
      (with-open [writer (io/writer file)]
        (json/generate-stream @all-questions writer)))

    (println "Done. Check" (.getAbsolutePath file))))
