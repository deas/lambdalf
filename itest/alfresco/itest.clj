;;
;; Copyright (C) 2011,2012 Carlo Sciolla
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns alfresco.itest
  (:use ;; [clojure.test]
        [clojure.tools.nrepl])
  (:require [clj-http.client :as http])
  (:import [it.sk.alfresco.clojure ContextHelper]))

;; (def lambdalf-url "http://localhost:9090/alfresco")
(def context-locations ["classpath:alfresco/application-context.xml" "classpath:alfresco/extension/no-jetty-ctx.xml"])
(defonce nrepl-started (atom false))
;; (defonce context-started (atom false))

(defn http-method
  "Resolves an HTTP method keyword such as :GET or :POST into its implementation fn. Input method is not case sensitive."
  [method]
   (->> method
        name
        (.toLowerCase)
       (symbol "clj-http.client")
       resolve))

;; (defn call-wscript
;;   ([lambdalf-url path]
;;      (call-wscript path :get))
;;   ([lambdalf-url path method]
;;      (let [method (http-method method)
;;            url (str lambdalf-url "/service" path)]
;;        (method url {:basic-auth ["admin" "admin"]}))))

;; (defn start-nrepl []
;;   (call-wscript "/clojure/nrepl" :post))

;; (defn ensure-nrepl
;;   "Ensures NREPL start is invoked only once"
;;   []
;;   (when (not @nrepl-started)
;;     (start-nrepl)
;;     (reset! nrepl-started true)))

;; Will go to integration tests
(defn create-application-context
  []
  (println "Initializing application context")
  (org.alfresco.util.ApplicationContextHelper/getApplicationContext (into-array String context-locations)))

(defn ensure-context
  "Ensures Application context is initialized only once"
  []
  (when (not (ContextHelper/getApplicationContext))
    (create-application-context))
  ;; (when (not @context-started)
  ;;   (create-application-context)
  ;;   (reset! context-started true))
  )

;; ; copied from NREPL test sources
;; (defmacro defftest
;;   "defines a test to be run against a remotely running nrepl server in a lambdalf instance"
;;   [name & body]
;;   `(deftest ~name
;;      (with-open [transport# (connect :port 7888)]
;;        (let [~'transport transport#
;;              ~'client (client transport# 10000)
;;              ~'session (client-session ~'client)
;;              ~'repl-eval #(message % {:op :eval :code %2})
;;              ~'repl-value (comp read-string :value first)
;;              ~'repl-values (comp response-values ~'repl-eval)]
;;          ~@body))))
