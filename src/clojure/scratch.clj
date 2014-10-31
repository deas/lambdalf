(ns scratch
  (:use [clojure.tools.namespace.repl :only [refresh]]
        [midje.repl])
  (:require [clojure.tools.logging :as log]
            [clojure.tools.namespace.find :as nsf]
            [clojure.repl :as repl]
            [contentreich.utils :as cu]
            [alfresco :as alf]
            [alfresco.auth  :as auth]
            [alfresco.search :as srch]
            [alfresco.nodes :as n]
            [alfresco.transact :as tx]
            [ring.adapter.jetty9 :refer [run-jetty]])
  (:import [org.alfresco.model ContentModel]))

;; A generic spring function should go into spring namespace
(defn create-application-context
  []
  (println "Initializing application context")
  (org.alfresco.util.ApplicationContextHelper/getApplicationContext (into-array String ["classpath:alfresco/application-context.xml" "classpath:alfresco/extension/no-jetty-ctx.xml"])))



(defn start-jetty
  "Starts jetty ... someday"
  []
  ;; (run-jetty app {:port 50505})
)

;; (def company-home
;;   (auth/as-admin
;;    (first
;;     (srch/query "PATH:\"/*\" AND TYPE:\"cm:folder\""))))
;;
;; (clojure.pprint/pprint
;;  (auth/as-admin
;;   (n/properties company-home)))
