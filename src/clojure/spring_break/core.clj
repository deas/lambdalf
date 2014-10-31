(ns spring-break.core
  (:require [clojure.tools.logging :as log]))
;; (defn log [fmt & args]
;;  (.println System/out (apply format (str "+++ " fmt) args)))

(defn -main [conf & bean-names]
  (let [_ (log/debug (format "Loading ClassPathXmlApplicationContext from '%s'" conf))
        closed (promise)
        sac (proxy [org.springframework.context.support.ClassPathXmlApplicationContext][conf]
              (close []
                (try
                  (log/debug "Shutting down Spring application context ...")
                  (proxy-super close)
                  (finally
                    (log/debug (format "Shutdown completed with %s."
                                       (if (proxy-super isActive)
                                         "FAIL/still active"
                                         "OK/inactive")))
                    (deliver closed :doesnt-matter)))))]
    ;;(.registerShutdownHook sac)
    (log/debug (format "Getting beans: [%s]" bean-names))
    (dorun
     (for [bean-id bean-names
           :let [bean (.getBean sac bean-id)]]
       (log/debug (format "bean '%s' = '%s'  (%s)"
                          bean-id
                          bean
                          (when bean (.getClass bean))))))
    (if (System/getProperty "wait-for-sac-close")
      (do
        (log/debug "Waiting for Spring application context shuttdown ...")
        @(promise))
      (.close sac))
    (log/debug "done.")))
