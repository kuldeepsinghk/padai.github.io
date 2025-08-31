(defproject math-quiz-server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :repositories [["clojars" "https://repo.clojars.org/"]
                 ["maven-central" "https://repo1.maven.org/maven2/"]
                 ;; You can add more custom Maven repositories here if needed
                 ;; ["my-custom-maven-repo" "https://my.company.com/maven-repo/"]
                 ]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-jetty-adapter "1.9.5"]
                 [cheshire "5.11.0"]
                 [clj-http "3.12.3"]
                 ]
  :main ^:skip-aot math-quiz-server.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})

