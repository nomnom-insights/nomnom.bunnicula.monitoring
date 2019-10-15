(ns bunnicula.monitoring-test
  (:require [bunnicula.monitoring :as monitoring]
            [bunnicula.protocol :as protocol]
            [stature.metrics.protocol :as metrics]
            [caliban.tracker.mock :as tracker]
            [clojure.test :refer :all]
            [com.stuartsierra.component :as component]))

(def counter-state (atom {}))

(defrecord FakeStatsd []
  component/Lifecycle
  (start [this] this)
  (stop [this]
    (reset! counter-state {})
    this)
  metrics/Metrics
  (count [this key]
    (swap! counter-state (fn [c] (update c key #(inc (get % key 0))))))
  (timing [this key val]
    (swap! counter-state #(assoc % (str key ".timing") val))))

(use-fixtures :each (fn [test-fn]
                      (reset! counter-state {})
                      (test-fn)))

(deftest monitoring-test
  (testing "monitoring with logging"
    (let [system (component/start-system
                  {:exception-tracker (tracker/create)
                   :statsd (->FakeStatsd)
                   :monitoring (component/using
                                (monitoring/create {:consumer-name "base.consumer"})
                                [:exception-tracker :statsd])})]
      (protocol/on-success (:monitoring system) {:queue-name "consumer-1"
                                                 :message "this is message"})
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
      (protocol/with-tracking (:monitoring system) (fn [] (Thread/sleep 13)))
      (is (= {"base.consumer.error"   1
              "base.consumer.fail"    1
              "base.consumer.retry"   1
              "base.consumer.success" 1
              "base.consumer.timeout" 1}
             (dissoc @counter-state "base.consumer.timing")))
      (is (>= (get @counter-state "base.consumer.timing")
              13))
      (component/stop-system system))))
(deftest log-setting-test
  (testing "monitoring without logging"
    (let [system (component/start-system
                  {:exception-tracker (tracker/create)
                   :statsd (->FakeStatsd)
                   :monitoring (component/using
                                (monitoring/create {:consumer-name "base.consumer-2"
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
      (protocol/with-tracking (:monitoring system) (fn [] (Thread/sleep 12)))
      (is (= {"base.consumer-2.error"   1
              "base.consumer-2.fail"    1
              "base.consumer-2.retry"   1
              "base.consumer-2.success" 1
              "base.consumer-2.timeout" 1}
             (dissoc @counter-state "base.consumer-2.timing")))
      (is (>= (get @counter-state "base.consumer-2.timing")
              12))
      (component/stop-system system))))
