(ns scratch
  (:require [clojure.tools.logging :as log]
            [alfresco.core :as core]))

(defn create-application-context
  []
  (println "Initializing application context")
  (org.alfresco.util.ApplicationContextHelper/getApplicationContext (into-array String ["classpath:alfresco/application-context.xml" "classpath:alfresco/extension/no-jetty-ctx.xml"])))
