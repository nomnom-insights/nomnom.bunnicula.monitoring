(defproject nomnom/bunnicula.monitoring "2.0.2"
  :description "Bunnicula: rabbitMQ client"
  :url "https://github.com/nomnom-insights/nomnom.bunnicula.monitoring"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [org.clojure/tools.logging "0.4.1"]
                 [nomnom/bunnicula "2.0.2"]
                 [nomnom/stature "1.0.5"]
                 [nomnom/caliban "1.0.2"]]
  :deploy-repositories {"clojars" {:sign-releases false}}
  :min-lein-version "2.5.0"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :year 2018
            :key "mit"}
  :profiles {:dev
             {:dependencies  [[ch.qos.logback/logback-classic "1.2.3"]]}})
