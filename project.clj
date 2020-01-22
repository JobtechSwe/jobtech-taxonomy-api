(defproject jobtech-taxonomy-api "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[buddy "2.0.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [cheshire "5.8.1"]
                 [clojure.java-time "0.3.2"]
                 [cprop "0.1.14"]
                 [expound "0.7.2"]
                 [funcool/struct "1.4.0"]
                 [luminus-immutant "0.2.5"]
                 [com.datomic/client-cloud "0.8.81"] ; for env/dev/
               ;;  [com.datomic/client-pro "0.9.41"]  ; for env/local/
;;                 [luminus-jetty "0.1.7"]
                 [luminus-transit "0.1.1"]
                 [luminus/ring-ttl-session "0.3.3"]
                 [markdown-clj "1.10.0"]
                 [metosin/muuntaja "0.6.4"]
                 [metosin/reitit "0.3.9"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [nrepl "0.6.0"]

                 [clj-http "3.10.0"]



                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "0.4.2"]
                 [org.clojure/tools.logging "0.5.0"]
                 [org.webjars.npm/bulma "0.7.5"]
                 [org.webjars.npm/material-icons "0.3.0"]
                 [org.webjars/webjars-locator "0.36"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [selmer "1.12.14"]
                 [metosin/ring-swagger "0.26.2"]
                 [jobtech-taxonomy-database "0.1.0-SNAPSHOT"]
                 [environ/environ.core "0.3.1"]]

;;  :dependencies [
;;                 [buddy "2.0.0"]
;;                 [camel-snake-kebab "0.4.0"]
;;                 [ch.qos.logback/logback-classic "1.2.3"]
;;                 [cheshire "5.8.1"]
;;                 [clj-time "0.15.0"]
;;                 [clojure.java-time "0.3.2"]
;;                 [com.datomic/client-cloud "0.8.71"] ; for env/dev/
;;                 [com.datomic/client-pro "0.8.28"]  ; for env/local/
;;                 [com.google.guava/guava "25.1-jre"]
;;                 [compojure "1.6.1"]
;;                 [cprop "0.1.14"]
;;                 [expound "0.7.2"]
;;                 [funcool/struct "1.4.0"]
;;                 [jobtech-nlp-stava "0.1.0"]
;;                 [jobtech-nlp-stop-words "0.1.0"]
;;                 [jobtech-nlp-tokeniser "0.1.0"]
;;                 [luminus-jetty "0.1.7"]
;;                 [luminus-immutant "0.2.4"]
;;                 [luminus-transit "0.1.1"]
;;                 [luminus/ring-ttl-session "0.3.3"]
;;                 [markdown-clj "1.10.0"]
;;                 [metosin/compojure-api "2.0.0-alpha28"]
;;                 [metosin/muuntaja "0.6.4"]
;;                 [metosin/reitit "0.3.9"]
;;                 [metosin/ring-http-response "0.9.1"]
;;                 [metosin/ring-swagger "0.26.2"]
;;                 [mount "0.1.16"]
;;                 [nrepl "0.6.0"]
;;                 [org.clojure/clojure "1.10.1"]
;;                 [org.clojure/data.json "0.2.6"]
;;                 [org.clojure/tools.cli "0.4.2"]
;;                 [org.clojure/tools.logging "0.5.0"]
;;                 [org.webjars.bower/tether "1.4.4"]
;;                 [org.webjars.npm/bulma "0.7.5"]
;;                 [org.webjars.npm/material-icons "0.3.0"]
;;                 [org.webjars/bootstrap "4.2.1"]
;;                 [org.webjars/font-awesome "5.6.1"]
;;                 [org.webjars/jquery "3.3.1-1"]
;;                 [org.webjars/webjars-locator "0.36"]
;;                 [ring-webjars "0.2.0"]
;;                 [ring/ring-core "1.7.1"]
;;                 [ring/ring-defaults "0.3.2"]
;;                 [ring/ring-json "0.4.0"]
;;                 [selmer "1.12.14"]
;;                 ]

  :min-lein-version "2.0.0"

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"

  :repositories [["snapshots" {:url "https://repo.clojars.org"
                               :username "batfish"
                               :password :env}]]

  :main ^:skip-aot jobtech-taxonomy-api.core
  :cljfmt {}

  :plugins []

  :profiles
  {
   :kaocha [:project/kaocha]

   :uberjar {:omit-source true
             :aot :all
             :uberjar-name "jobtech-taxonomy-api.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]
             }

   :dev           [:project/dev :profiles/dev]
   :local         [:project/local :profiles/local]
   :test          [:project/test :profiles/test]

   :project/kaocha {:dependencies [[lambdaisland/kaocha "0.0-418"]]
                    ;; You can only comment in one resource-path:
                    ;; :resource-paths ["env/dev/resources"] ; comment in for local use
                    :resource-paths ["env/integration-test/resources"] ; comment in for Jenkins
                    }
   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn" ; FIXME: the filed referred here does not exist
                             ]
                  :dependencies [[expound "0.7.2"]
                                 [lambdaisland/kaocha "0.0-418"]
                                 [pjstadig/humane-test-output "0.9.0"]
                                 [prone "1.6.1"]
                                 [ring/ring-devel "1.7.1"]
                                 [ring/ring-mock "0.4.0"]]
                  ;;:plugins      [[com.jakemccrary/lein-test-refresh "0.24.0"]]

                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/local {:dependencies [[expound "0.7.2"]
                                  [pjstadig/humane-test-output "0.9.0"]
                                  [prone "1.6.1"]
                                  [ring/ring-devel "1.7.1"]
                                  [ring/ring-mock "0.3.2"]]
                   ;;:plugins      [[com.jakemccrary/lein-test-refresh "0.23.0"]]
                   :source-paths ["env/local/clj"]
                   :resource-paths ["env/local/resources"]
                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]
                   :repl-options {:init-ns user}}
   :project/test {:jvm-opts ["-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]}
   :profiles/dev {}
   :profiles/local {}
   :profiles/test  {}
   }
  :aliases {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]}
  )
