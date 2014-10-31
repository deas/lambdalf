(ns spring-break.proxying
  (:require [clojure.tools.logging :as log]))

(def my-interceptor
  (proxy [org.aopalliance.intercept.MethodInterceptor][]
    (invoke [invctn]
      (let [m (.getMethod invctn)
            t (.getThis invctn)
            a (vec (.getArguments invctn))
            _ (log/debug (format "+++ Calling method %s on %s with %s" m t a))
            res (try
                  {:res (.proceed invctn)}
                  (catch Throwable t {:ex t}))]
        (log/debug (format "+++ DONE: Calling method %s on %s with %s %s"
                           m t a
                           (if-let [r (:res res)]
                             (format "returns '%s'" r)
                             (format "fails due to %s" (:ex res)))))
        (if-let [r (:res res)]
          r
          (throw (:ex res)))))))
