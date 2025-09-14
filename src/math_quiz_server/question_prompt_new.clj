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

;; Helper function to get example from schema JSON
(defn get-schema-example
  "Gets an example directly from the schema JSON file without formatting"
  []
  ;; Get the first example and convert it directly to JSON string
  (let [example (-> question-schema-json :examples first first)]
    (json/generate-string example {:pretty true})))

;; Helper function for validating required fields
(defn- has-required-fields?
  "Checks if a question has all required fields"
  [question]
  (and (contains? question :category)
       (contains? question :question)
       (contains? question :options)
       (contains? question :correct)
       (contains? question :rationale)))

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
        example-json (get-schema-example)
        min-options 4
        
        ;; Break the prompt into logical sections
        intro-section (str "You are an experienced Indian CBSE Class " grade-str 
                          " and " subject-str " teacher.\n"
                          "Your task is to generate practice questions for students studying the NCERT syllabus.\n\n")
        
        task-section (str "Generate " num-questions " multiple-choice math questions "
                         "for grade " grade-str " students studying " subject-str ", "
                         "specifically on the topic of " topic-str ". "
                         "Provide a mix of easy, medium, and Hard problems. ")
        
        format-section (str "Format your response as a valid JSON array with the following structure for each question:\n\n"
                           example-json "\n\n")
        
        rules-section (str "Rules:\n"
                          "1. Each question must have EXACTLY " min-options " options\n"
                          "2. Make sure questions are appropriate for grade " grade-str " students\n"
                          "3. Include clear rationales that explain the solution process\n"
                          "4. Ensure the JSON is valid and follows the schema exactly\n"
                          "5. Make sure the topic \"" topic-str "\" is clearly related to all questions\n"
                          "6. Do not use LaTeX syntax. Use a standard forward slash (e.g., 1/4) for fractions")]
    
    ;; Combine sections
    (str intro-section task-section format-section rules-section)))

(defn validate-llm-response
  "Validates the LLM JSON response and filters out invalid elements.
   
   Validation steps:
   1. Checks that the input is valid JSON
   2. Ensures it's an array structure
   3. Filters to keep only elements with all required fields: 
      category, question, options, correct, and rationale
   
   Parameters:
   - response: String JSON response from the LLM
   
   Returns:
   - If valid JSON: A vector containing only valid question elements (may be empty)
   - If invalid JSON: nil and prints error message"
  [response]
  (try
    (let [parsed-data (json/parse-string response true)]
      (if-not (sequential? parsed-data)
        (do
          (println "Invalid response format: Not an array - Expected array of questions")
          nil)
        
        ;; Process valid data using threading macro for cleaner data flow
        (let [valid-elements (->> parsed-data
                                  (filter has-required-fields?)
                                  (vec))
              filtered-count (- (count parsed-data) (count valid-elements))]
          
          ;; Log only if we filtered anything out
          (when (pos? filtered-count)
            (println "Filtered out" filtered-count "invalid elements that were missing required fields"))
          
          valid-elements)))
    
    (catch Exception e
      (println "Error parsing LLM response: Not valid JSON -" (.getMessage e))
      nil)))
