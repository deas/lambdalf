(ns scratch
  (:use [clojure.tools.namespace.repl :only [refresh]])
  (:require [clojure.tools.logging :as log]
            [clojure.tools.namespace.find :as nsf]
            [clojure.repl :as repl]
            [alfresco :as alf]
            [alfresco.auth  :as auth]
            [alfresco.transact :as tx]
            [ring.adapter.jetty9 :refer [run-jetty]])
  (:import [org.alfresco.model ContentModel]))

(defn create-application-context
  []
  (println "Initializing application context")
  (org.alfresco.util.ApplicationContextHelper/getApplicationContext (into-array String ["classpath:alfresco/application-context.xml" "classpath:alfresco/extension/no-jetty-ctx.xml"])))

(defn start-jetty
  "Starts jetty ... someday"
  []
  ;; (run-jetty app {:port 50505})
)
