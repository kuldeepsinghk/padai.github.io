(ns math-quiz-server.constants
  (:require [math-quiz-server.models :refer [create-curriculum-spec]]))

;; Define topics by grade and subject
(def topics-by-grade-and-subject
  {;; 6th grade topicsclea
   6 {"Math" ["Basic Arithmetic" "Decimals" "Fractions" "Geometry" "Measurement"]
      "Science" ["Plants"]}
   
   ;; 7th grade topics
   7 {"Math" ["LCM and HCF" "Whole Numbers" "Integers" "Number Line" "Prime Number" "Fractions"]
      "Science" ["Matter" "Force" "Energy" "Light" "Sound" "Electricity"]}
   
   ;; 10th grade topics
   10 {
;;        "Math" ["Real Numbers" "Polynomials" ]
;;        "Physics" ["Light:Reflection" "Light:Refraction" "The Human Eye and the Colourful World" "Electricity" "Magnetic Effects of Electric Current"]
;;        "Physics" ["Light:Reflection" "Light:Refraction" "The Human Eye and the Colourful World"]
;;        "chemistry" ["Chemical Reactions and Equations" "Acids, Bases, and Salts" "Metals and Non-metals" "Carbon and Its Compounds"]
       "chemistry" ["Chemical Reactions and Equations" "Acids, Bases, and Salts"]
;;        "biology" ["Life Processes" "Control and Coordination" "How Do Organisms Reproduce" "Heredity and Evolution" "Our Environment"]
       "biology" ["Life Processes" "Control and Coordination" "How Do Organisms Reproduce"]
       }})

;; Function to create a curriculum spec for a specific grade
(defn get-grade-curriculum [grade]
  (let [subjects (keys (get topics-by-grade-and-subject grade))
        subject-topics (get topics-by-grade-and-subject grade)]
    (create-curriculum-spec grade subjects subject-topics)))

;; Pre-defined curriculum specs
(def grade-6-curriculum (get-grade-curriculum 6))
(def grade-7-curriculum (get-grade-curriculum 7))
(def grade-10-curriculum (get-grade-curriculum 10))

;; Alternatively, access them dynamically
(defn curriculum-for-grade [grade]
  (if (contains? topics-by-grade-and-subject grade)
    (get-grade-curriculum grade)
    (throw (ex-info (str "No curriculum defined for grade " grade) 
                    {:grade grade :available-grades (keys topics-by-grade-and-subject)}))))
