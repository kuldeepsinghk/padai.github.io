(ns math-quiz-server.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :as resp]
            [ring.middleware.resource :refer [wrap-resource]])
  (:gen-class))



(def handler
  (wrap-resource (fn [request] (resp/not-found "Not Found")) "public"))

(defn -main
  "Starts the web server."
  [& args]
  (let [port 8000]
    (println (str "Starting server on http://localhost:" port))
    (run-jetty handler {:port port :join? false})))
