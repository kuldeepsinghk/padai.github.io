(ns math-quiz-server.question-generator-new
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [math-quiz-server.question-prompt-new :as prompt]
            [math-quiz-server.question-gen :as curr]))

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

(defn save-questions-to-file
  "Saves generated questions to a JSON file.
   
   Parameters:
   - questions: Vector of question objects
   - grade: Keyword representing the grade level
   - subject: Keyword representing the subject
   
   Returns:
   - Path to the saved file"
  [questions grade subject]
  (let [grade-str (if (keyword? grade) (name grade) grade)
        subject-str (if (keyword? subject) (name subject) subject)
        file-name (str "resources/public/quiz-data-" grade-str "-" subject-str ".json")
        file (io/file file-name)]
    ;; Create directories if they don't exist
    (io/make-parents file)
    
    ;; Write questions to file with pretty formatting
    (with-open [writer (io/writer file)]
      (json/generate-stream questions writer {:pretty true}))
    
    ;; Return the path to the file
    (.getAbsolutePath file)))

(defn generate-questions-by-curriculum
  "Generates questions for all topics in a curriculum for a given grade and subject.
   Saves the generated questions to a file named quiz-data-{grade}-{subject}.json

 Parameters:
 - grade: Keyword representing the grade level (:grade-6, :grade-7, etc.)
 - subject: Keyword representing the subject (:Math, :Science, etc.)
 - save-to-file?: Boolean flag to indicate whether to save questions to file
 - num-questions: Number of questions to generate per topic (default: 2)

 Returns:
 - If save-to-file? is true: A map with :questions (the question vector) and :file-path
 - If save-to-file? is false: Just the vector of question objects
 - If error: nil"
    ([grade subject save-to-file?]
     (generate-questions-by-curriculum grade subject save-to-file? 2))
    ([grade subject save-to-file? num-questions]
     (try
       ;; Get all topics for the given grade and subject
       (let [topics (curr/get-curriculum-items grade subject)]
         (if (seq topics)
           (let [;; Generate questions for each topic
                 questions (->> topics
                                (mapcat #(generate-questions grade subject % num-questions))
                                (filter identity)  ; Remove any nil results
                                vec)]
             ;; Save to file if requested
             (if save-to-file?
               (let [file-path (save-questions-to-file questions grade subject)]
                 {:questions questions
                  :file-path file-path})
               questions))
           ;; No topics found
           (do
             (println "No topics found for grade" grade "and subject" subject)
             (if save-to-file? {:questions [] :file-path nil} []))))
       (catch Exception e
         (println "Error generating questions by curriculum:" (.getMessage e))
         nil))))