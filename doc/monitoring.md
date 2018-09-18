# Monitoring component

<img src="http://www.gstatic.com/tv/thumb/tvbanners/12567255/p12567255_b_v8_aa.jpg" align="right"  height="200px" />

Monitoring component which can be used for consumers defined by [bunnicula library](https://github.com/nomnom-insights/nomnom.bunnicula)

Each time consumer is processing message it will
- track the processing time and send metric to StatsD
- log the result of processing and send metric to StatsD
- in case of exception it will report the exception to Rollbar


### Component configuration

- **consumer-name** is used in log messages and to create metrics key
- **log-message-size:** (optional, default 100) specify how much of the parsed message data you want to appear in log.
 Set to 0 if you don't want to log any message data.

### Component dependencies
The [exception-tracker](https://github.com/nomnom-insights/nomnom.caliban) and [statsd](https://github.com/nomnom-insights/nomnom.stature) component
 are required dependencies, they have to be present under `:exception-tracker` and `:statsd` key in the system map.

### Metric keys

Metric key is formatted as *\[prefix\].\[consumer-name\].\[result\]*.

Assume statsd component was created with `prefix` set to *my-server*
and monitoring component was created with `consumer-name` equal to *some.queue-1*.

The metric key used to record timing is *my-server.some.queue-1*
and the keys used to record result are *my-server.some.queue-1.success*, *my-server.some.queue-1.retry* etc.



### Usage

```clojure

(require '[bunnicula.monitoring :as monitoring]
         '[bunnicula.component.connection :as connection ]
         '[bunnicula.component.consumer-with-retry :as consumer ]
         '[com.stuartsierra.component :as component]
         '[caliban.tracker :as tracker]
         '[stature.metrics :as metrics])


(def tracker (tracker/create {:token "123"
                              :environment "production"}))

(def statsd (metrics/create {:host "localhost"
                             :port 8125
                             :prefix "my-server"}))

(def connection (connection/create {:url "amqp://rabbit:passw0rd@127.0.0.1:5672"
                                    :vhost "/main"}))

(def consumer-1 (consumer/create {:message-handler-fn (fn [& args] :ack)
                                  :options {:queue-name "some.queue-1"
                                            :exchange-name "my-exchange"}}))

(def consumer-2 (consumer/create {:message-handler-fn (fn [& args] :ack)
                                  :options {:queue-name "some.queue-2"
                                            :exchange-name "my-exchange"}}))

(def system (-> (component/system-map
                  :exception-tracker tracker
                  :statsd statsd
                  :rmq-connection connection
                  :monitoring-1 (component/using
                                  (monitoring/create {:consumer-name "some.queue-1"})
                                  [:exception-tracker :statsd])
                  :consumer-1 (component/using
                                consumer-1
                                {:rmq-connection :rmq-connection
                                 :monitoring :monitoring-1})
                  :monitoring-2 (component/using
                                  (monitoring/create {:consumer-name "some.queue-1"})
                                  [:exception-tracker :statsd])
                  :consumer-2 (component/using
                                consumer-2
                                {:rmq-connection :rmq-connection
                                 :monitoring :monitoring-2}))
                component/start-system))
```


### Logging

Monitoring component depends on `clojure.tools.logging`.
Log formatting needs to be configured. See [example](https://gist.github.com/lukaszkorecki/a57deda7190d42d61cc373b665a0faa2)
using `ch.qos.logback/logback-classic` library.