(ns spring-break.lifecycle
  (:require [clojure.tools.logging :as log]))

(defprotocol some-bean-java-bean
  (setFoo [this v])
  (setBar [this v]))

(def some-bean
  (let [state (atom {})]
    (reify
      some-bean-java-bean
      org.springframework.beans.factory.InitializingBean
      org.springframework.beans.factory.DisposableBean
      org.springframework.context.SmartLifecycle

      (toString [this] (str @state))

      (setFoo [this v]
        (swap! state assoc :foo v)
        (log/debug (format "+++ after setFoo : %s" this)))
      (setBar [this v]
        (swap! state assoc :bar v)
        (log/debug (format "+++ after setBar : %s" this)))

      ;; org.springframework.beans.factory.InitializingBean
      (afterPropertiesSet [this] (log/debug (format "+++ afterPropertiesSet : %s" this)))

      ;; org.springframework.beans.factory.DisposableBean
      (destroy [this] (log/debug (format "+++ destroy : %s\n" this)))

      ;; org.springframework.context.SmartLifecycle
      (getPhase [this] 0)
      (isAutoStartup [this] (log/debug (format "+++ isAutoStartup : %s" this)) true)
      (isRunning [this] (log/debug (format "+++ isRunning : %s" this)) true)
      (start [this] (log/debug (format "+++ start : %s" this)))
      (stop [this runnable] (log/debug (format "+++ stop : %s\n" this)) (.run runnable))
      )))
