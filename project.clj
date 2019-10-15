(defproject nomnom/bunnicula.monitoring "2.1.0"
  :description "Monitoring middleware for Bunnicula, with built in support for StatsD metrics and error reporting to Rollbar"
  :url "https://github.com/nomnom-insights/nomnom.bunnicula.monitoring"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.stuartsierra/component "0.4.0"]
                 [org.clojure/tools.logging "0.5.0"]
                 [nomnom/bunnicula "2.1.0"]
                 [nomnom/stature "2.0.0"]
                 [nomnom/caliban "1.0.2"]]
  :deploy-repositories {"clojars" {:sign-releases false}}
  :min-lein-version "2.5.0"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :year 2018
            :key "mit"}
  :profiles {:dev
             {:dependencies  [[ch.qos.logback/logback-classic "1.2.3"]]}})
