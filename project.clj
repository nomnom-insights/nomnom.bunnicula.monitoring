(defproject nomnom/bunnicula.monitoring "2.1.0"
  :description "Monitoring middleware for Bunnicula, with built in support for StatsD metrics and error reporting to Rollbar"
  :url "https://github.com/nomnom-insights/nomnom.bunnicula.monitoring"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [com.stuartsierra/component "1.0.0"]
                 [org.clojure/tools.logging "1.1.0"]
                 [nomnom/bunnicula "2.2.3-SNAPSHOT-1"]
                 [nomnom/stature "2.0.1-SNAPSHOT-1"]
                 [nomnom/caliban "1.0.3-SNAPSHOT-1"]]
  :deploy-repositories {"clojars" {:sign-releases false}}
  :min-lein-version "2.5.0"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :year 2018
            :key "mit"}
  :global-vars {*warn-on-reflection* true}
  :profiles {:dev
             {:dependencies  [[ch.qos.logback/logback-classic "1.2.7"]]}})
