(ns bunnicula.monitoring-test
  (:require [bunnicula.monitoring :refer :all]
            [bunnicula.protocol :as protocol]
            [stature.metrics.protocol :as metrics]
            [caliban.tracker.mock :as tracker]
            [clojure.test :refer :all]
            [com.stuartsierra.component :as component]))

(defrecord FakeStatsd [counter]
  component/Lifecycle
  (start [this]
    (assoc this :counter (atom {})))
  (stop [this]
    (assoc this :counter nil))
  metrics/Metrics
  (count [this key]
    (swap! counter #(assoc % key "count")))
  (timing [this key val]
    (swap! counter #(assoc % key val))))

(deftest monitoring-test
  (testing "monitoring with logging"
    (let [system (component/start-system
                   {:exception-tracker (tracker/create)
                    :statsd (map->FakeStatsd {})
                    :monitoring (component/using
                                  (create {:consumer-name "base"})
                                  [:exception-tracker :statsd])})]
      (protocol/on-success (:monitoring system) {:queue-name "consumer-1"
                                                 :message "this is message"})
      (protocol/on-error (:monitoring system) {:queue-name "consumer-1"
                                               :message "this is message"})
      (protocol/on-timeout (:monitoring system) {:queue-name "consumer-1"
                                                 :message "this is message"})
      (protocol/on-retry (:monitoring system) {:queue-name "consumer-1"
                                               :retry-attempts 2
                                               :message "this is message"})
      (protocol/on-exception (:monitoring system) {:queue-name "consumer-1"
                                                   :message "this is message"
                                                   :exception (Exception. "FAILED")})
      (protocol/with-tracking (:monitoring system) (fn [] (Thread/sleep 1)))
      (is (= {"base.error"   "count"
              "base.fail"    "count"
              "base.retry"   "count"
              "base.success" "count"
              "base.timeout" "count"
              "base" 1}
             @(:counter (:statsd system))))))
  (testing "monitoring without logging"
    (let [system (component/start-system
                   {:exception-tracker (tracker/create)
                    :statsd (map->FakeStatsd {})
                    :monitoring (component/using
                                  (create {:consumer-name "base-2"
                                           :log-message-size 0})
                                  [:exception-tracker :statsd])})]
      (protocol/on-success (:monitoring system) {:queue-name "consumer-1"
                                                 :message "this is message"})
      (protocol/on-error (:monitoring system) {:queue-name "consumer-1"
                                               :message "this is message"})
      (protocol/on-timeout (:monitoring system) {:queue-name "consumer-1"
                                                 :message "this is message"})
      (protocol/on-retry (:monitoring system) {:queue-name "consumer-1"
                                               :retry-attempts 2
                                               :message "this is message"})
      (protocol/on-exception (:monitoring system) {:queue-name "consumer-1"
                                                   :message "this is message"
                                                   :exception (Exception. "FAILED")})
      (protocol/with-tracking (:monitoring system) (fn [] (Thread/sleep 1))))))