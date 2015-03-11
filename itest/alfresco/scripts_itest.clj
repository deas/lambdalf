(ns alfresco.scripts_itest
  (:use [midje.sweet])
  (:require [alfresco.auth :as auth]
            [alfresco.nodes :as n]
            [alfresco.core :as c]
            [alfresco.transact :as tx]
            [alfresco.search :as srch]
            [alfresco.itest :refer [ensure-context]]))

(background (before :facts (ensure-context)))

(facts "About Scripts" :it
  (fact "When we execute a string as clj, what goes in must come out (company-home)" :it
    (let [script-service (c/get-bean "ScriptService")
          company-home (auth/as-admin
                        (first
                         (srch/query "PATH:\"/*\" AND TYPE:\"cm:folder\"")))
          script-model (doto (java.util.HashMap.)
                         (.put "document" company-home))
          clj-script "(require '[spring.surf.webscript :as w])(w/create-script (fn [] document))"]
      (= company-home (.executeScriptString script-service "clojure" clj-script script-model))) => true
    )
  )
