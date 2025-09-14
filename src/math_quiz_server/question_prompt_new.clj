(ns math-quiz-server.question-prompt-new
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [malli.core :as m]))

;; Load the question schema directly from JSON file
(def question-schema-json
  (try
    (let [schema-resource (io/resource "question_schema.json")]
      (if schema-resource
        (json/parse-string (slurp schema-resource) true)
        (do
          (println "Warning: question_schema.json not found in resources")
          nil)))
    (catch Exception e
      (println "Error loading question schema:" (.getMessage e))
      nil)))

;; Define the question schema using Malli based on loaded JSON schema
(def question-schema
  [:vector
   [:map
    [:category string?]
    [:question string?]
    [:options [:vector string? {:min (-> question-schema-json :items :properties :options :minItems)
                                :max (-> question-schema-json :items :properties :options :maxItems)}]]
    [:correct [:int {:min (-> question-schema-json :items :properties :correct :minimum)
                     :max (-> question-schema-json :items :properties :correct :maximum)}]]
    [:rationale string?]]])

;; Helper function to get example from schema JSON
(defn get-schema-example
  "Gets an example directly from the schema JSON file without formatting"
  []
  ;; Get the first example and convert it directly to JSON string
  (let [example (-> question-schema-json :examples first first)]
    (json/generate-string example {:pretty true})))

(defn create-gemini-prompt
  "Creates a prompt for Gemini LLM to generate math questions.
   
   Parameters:
   - grade: Keyword representing the grade level (:grade-6, :grade-7, etc.)
   - subject: Keyword representing the subject (:Math, :Science, etc.)
   - topic: Keyword representing the specific topic
   - num-questions: Number of questions to generate"
  [grade subject topic num-questions]
  (let [grade-str (subs (name grade) 6) ; Remove 'grade-' prefix
        subject-str (name subject)
        topic-str (name topic)
        example-json (get-schema-example) ; No longer passing topic
        min-options 4]
    
    (str "Generate " num-questions " multiple-choice math questions for grade " grade-str 
         " students studying " subject-str ", specifically on the topic of " topic-str ". "
         "Format your response as a valid JSON array with the following structure for each question:\n\n"
         example-json "\n\n"
         "Rules:\n"
         "1. Each question must have EXACTLY " min-options " options\n"
         "2. Make sure questions are appropriate for grade " grade-str " students\n"
         "3. Include clear rationales that explain the solution process\n"
         "4. Ensure the JSON is valid and follows the schema exactly\n"
         "5. Make sure the topic \"" topic-str "\" is clearly related to all questions")))

(defn validate-llm-response
  "Validates if the LLM response is valid JSON and has correct structure.
   
   Parameters:
   - response: String JSON response from the LLM
   
   Returns:
   - If valid: The parsed questions as a Clojure data structure (vector)
   - If invalid: nil and prints error message"
  [response]
  (try
    ;; Parse the JSON string into Clojure data
    (let [parsed-data (json/parse-string response true)
          ;; Ensure we have a vector (convert from LazySeq if needed)
          data-vec (vec parsed-data)]
      
      ;; Step 1: Check if it's a sequential collection (vector, list, seq, etc.)
      (if (sequential? parsed-data)
        ;; Step 2: Check each item has required fields
        (if (every? #(contains? % :category) data-vec)
          ;; Valid format with required fields
          data-vec
          ;; Missing required fields
          (do
            (println "Invalid response format: One or more questions missing required 'category' field")
            nil))
        ;; Not a sequential collection
        (do
          (println "Invalid response format: Not an array - Expected array of questions")
          nil)))
    (catch Exception e
      (println "Error parsing LLM response: Not valid JSON -" (.getMessage e))
      nil)))
