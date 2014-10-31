(ns spring-break.application-context-aware
  (:require [clojure.tools.logging :as log]))

(defn consume-args [sac args]
  (reduce-kv #(assoc %1 %2 (.getBean sac (name %3)))
             {}
             (apply hash-map args)))

(defn make-some-bean [& args]
  (log/debug (format "+++ make-some-bean args = %s" args))
  (let [state (atom {})]
    (reify
      org.springframework.context.ApplicationContextAware
      org.springframework.beans.factory.InitializingBean
      org.springframework.beans.factory.DisposableBean
      org.springframework.context.SmartLifecycle

      (toString [this] (str @state))

      ;; org.springframework.context.ApplicationContextAware
      (setApplicationContext [this v]
        (swap! state assoc :sac v)
        (log/debug (format "+++ after setApplicationContext : %s" this)))

      ;; org.springframework.beans.factory.InitializingBean
      (afterPropertiesSet [this]
        ;;(reset! state (consume-args (:sac @state) args))
        (swap! state merge (consume-args (:sac @state) args))
        (log/debug (format "+++ afterPropertiesSet : %s" this)))

      ;; org.springframework.beans.factory.DisposableBean
      (destroy [this] (log/debug (format "+++ destroy : %s" this))
        #_ @(promise))

      ;; org.springframework.context.SmartLifecycle
      (getPhase [this] 0)
      (isAutoStartup [this] (log/debug (format "+++ isAutoStartup : %s" this)) true)
      (isRunning [this] (log/debug (format "+++ isRunning : %s" this)) true)
      (start [this] (log/debug (format "+++ start : %s" this)))
      (stop [this runnable] (log/debug (format "+++ stop : %s" this)) (.run runnable))
      )))
