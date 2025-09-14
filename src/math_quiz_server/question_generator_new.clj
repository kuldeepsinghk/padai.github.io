(ns math-quiz-server.question-generator-new
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.string :as string]
            [math-quiz-server.question-prompt-new :as prompt]))

(def api-key
  (or (System/getenv "GEMINI_API_KEY")
      (throw (Exception. "GEMINI_API_KEY environment variable not set"))))

(def gemini-api-url "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")

(defn clean-json-text
  "Cleans the LLM response text to extract just the JSON content"
  [text]
  (-> text
      (string/replace #"```json\s*" "")  ; Remove opening ```json
      (string/replace #"```\s*" "")      ; Remove closing ```
      (string/trim)))                    ; Remove any extra whitespace

(defn generate-questions
  "Generates questions using the improved prompt generator.
   
   Parameters:
   - grade: Keyword representing the grade level (:grade-6, :grade-7, etc.)
   - subject: Keyword representing the subject (:Math, :Science, etc.)
   - topic: Keyword or string representing the specific topic
   - num-questions: Number of questions to generate
   
   Returns:
   - A vector of valid question objects, or nil if there was an error"
  [grade subject topic num-questions]
  (let [topic-kw (if (keyword? topic) topic (keyword topic))
        prompt (prompt/create-gemini-prompt grade subject topic-kw num-questions)]
    (try
      (let [request-body (json/encode {:contents [{:parts [{:text prompt}]}]})
            response (client/post gemini-api-url
                                  {:headers {"Content-Type" "application/json"}
                                   :query-params {:key api-key}
                                   :body request-body
                                   :as :json})
            raw-text (-> response :body :candidates first :content :parts first :text)
            cleaned-json (clean-json-text raw-text)]
        
        ;; Use our improved validator to filter out invalid questions
        (prompt/validate-llm-response cleaned-json))
      (catch Exception e
        (println "Error generating questions:" (.getMessage e))
        nil))))
