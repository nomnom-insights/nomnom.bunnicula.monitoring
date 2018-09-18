(ns bunnicula.monitoring
  (:require [bunnicula.protocol :as proto]
            [caliban.tracker.protocol :as tracker]
            [clojure.tools.logging :as log]
            [stature.metrics.protocol :as stature]
            [stature.metrics :as metrics]
            [com.stuartsierra.component :as component]))

(defn- create-log-fn
  "Return fn to be used to format consumer messages for logging.
   If log-message-size is 0, do not log message."
  [log-message-size]
  (fn [message]
    (if (> log-message-size 0)
      (let [s (str message)]
        (subs s 0 (min log-message-size (count s))))
      "logging-message-disabled")))

(defn count-result
  [statsd result consumer-name]
  (let [metric-key (format "%s.%s" consumer-name (name result))]
    (stature/count statsd metric-key)))

(defrecord Monitoring [consumer-name log-message-size exception-tracker statsd log-fn]
  component/Lifecycle
  (start [c]
    (log/infof "bunnicula-monitoring start consumer-name=%s log-message-size=%s"
               consumer-name log-message-size)
    (assoc c :log-fn (create-log-fn log-message-size)))

  (stop [c]
    (log/infof "bunnicula-monitoring stop consumer-name=%s" consumer-name)
    (assoc c :log-fn nil))
  
  proto/Monitoring
  (with-tracking [this message-fn]
    (metrics/with-timing statsd consumer-name (message-fn)))
  (on-success [this args]
    (log/infof "consumer=%s success" consumer-name)
    (count-result statsd :success consumer-name))
  (on-error [this args]
    (log/errorf "consumer=%s error payload=%s"
                consumer-name (log-fn (:message args)))
    (count-result statsd :error consumer-name))
  (on-timeout [this args]
    (log/errorf "consumer=%s timeout payload=%s"
                consumer-name (log-fn (:message args)))
    (count-result statsd :timeout consumer-name))
  (on-retry [this args]
    (log/errorf "consumer=%s retry-attempts=%d payload=%s"
                consumer-name (:retry-attempts args) (log-fn (:message args)))
    (count-result statsd :retry consumer-name))
  (on-exception [this args]
    (let [{:keys [exception message]} args]
      (log/errorf exception "consumer=%s exception payload=%s"
                  consumer-name (log-fn message))
      (when exception-tracker
        (tracker/report exception-tracker exception)))
    (count-result statsd :fail consumer-name)))


(defn create
  "Create component to be used for monitoring bunnicula consumers.
   Each result is logged & reported to statsd.
   Exceptions are tracked with exception-tracker (if present as dependency)
   Args
   - consumer-name: consumer-name (used for logging and metrics)
   - log-message-size: trim message-data to given size
      (default 100, set to 0 t disable logging consumer message)"
  [{:keys [log-message-size consumer-name]
    :or {log-message-size 100}}]
  {:pre [(string? consumer-name)
         (integer? log-message-size)]}
  (map->Monitoring {:consumer-name consumer-name
                    :log-message-size log-message-size}))
