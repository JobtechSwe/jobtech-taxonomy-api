(defproject jobtech-taxonomy-api "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[jobtech-taxonomy-database "0.1.0-SNAPSHOT"]
                 [buddy "2.0.0"]
                 [cheshire "5.8.1"]
                 [clojure.java-time "0.3.2"]
                 [org.clojure/data.json "0.2.6"]
                 [com.datomic/client-cloud "0.8.71"] ; for env/dev/
                 [com.datomic/client-pro "0.8.28"]  ; for env/local/
                 [clj-time "0.15.0"]
                 [com.google.guava/guava "25.1-jre"]
                 [compojure "1.6.1"]
                 [cprop "0.1.13"]
                 [funcool/struct "1.3.0"]
                 [luminus-immutant "0.2.4"]
                 [luminus-transit "0.1.1"]
                 [luminus/ring-ttl-session "0.3.2"]
                 [markdown-clj "1.0.5"]
                 [metosin/compojure-api "2.0.0-alpha28"]
                 [metosin/muuntaja "0.6.3"]
                 [metosin/ring-http-response "0.9.1"]
                 [lambdaisland/kaocha "0.0-418"]
                 [mount "0.1.15"]
                 [nrepl "0.5.3"]
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.cli "0.4.1"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.webjars.bower/tether "1.4.4"]
                 [org.webjars/bootstrap "4.2.1"]
                 [org.webjars/font-awesome "5.6.1"]
                 [org.webjars/jquery "3.3.1-1"]
                 [org.webjars/webjars-locator "0.34"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.4.0"]
                 [selmer "1.12.5"]
                 [org.clojure/tools.logging "0.4.1"]
                 [camel-snake-kebab "0.4.0"]
                 ]

  :min-lein-version "2.0.0"

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ^:skip-aot jobtech-taxonomy-api.core
  :cljfmt {}

  :plugins [[lein-immutant "2.1.0"]
            [lein-kibit "0.1.2"]
            [lein-cljfmt "0.6.3"]]
  :profiles
  {
   :kaocha [:project/kaocha]

   :uberjar {:omit-source true
             :aot :all
             :uberjar-name "jobtech-taxonomy-api.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]
             :jvm-opts [~(str "-Djava.library.path=" (System/getenv "HOME") "/.clj-nativedep/jobtech-nlp-stava/0.1.0/linux-amd64")]}

   :dev           [:project/dev :profiles/dev]
   :local         [:project/local :profiles/local]
   :test          [:project/test :profiles/test]

   :project/kaocha {:dependencies [[lambdaisland/kaocha "0.0-418"]]
                    :jvm-opts [~(str "-Djava.library.path=" (System/getenv "HOME") "/.clj-nativedep/jobtech-nlp-stava/0.1.0/linux-amd64")]
                    ;; You can only comment in one resource-path:
                    ;:resource-paths ["env/dev/resources"] ; comment in for local use
                    :resource-paths ["env/integration-test/resources"] ; comment in for Jenkins
                    }
   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn" ; FIXME: the filed referred here does not exist
                             ~(str "-Djava.library.path=" (System/getenv "HOME") "/.clj-nativedep/jobtech-nlp-stava/0.1.0/linux-amd64")]
                  :dependencies [[expound "0.7.2"]
                                 [pjstadig/humane-test-output "0.9.0"]
                                 [prone "1.6.1"]
                                 [ring/ring-devel "1.7.1"]
                                 [ring/ring-mock "0.3.2"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.23.0"]]

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
                   :plugins      [[com.jakemccrary/lein-test-refresh "0.23.0"]]
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
