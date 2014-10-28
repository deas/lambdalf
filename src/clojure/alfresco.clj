;;
;; Copyright (C) 2011,2012 Carlo Sciolla, Peter Monks
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


(ns alfresco
  (:require [clojure.tools.nrepl.server :as nrepl]
            [clojure.tools.logging :as log]
            [cider.nrepl :refer (cider-nrepl-handler)]
            [clojure.java.classpath :as cp]
            [clojure.tools.namespace.find :as nsf]))

; Hold a reference to the NREPL server
(def ^:private nrepl-server (atom nil))

(defn load-namespaces
  "Load namespaces by regular expression names. Returns their symbols"
  ([ns-regexps]
     (log/info "Requiring namespaces ...")
     (let [re (map re-pattern ns-regexps)]
       (->> (nsf/find-namespaces (cp/classpath))
            (filter (fn[sym] (some (fn[re] (re-matches re (name sym))) re)) ,)
            (map (fn[sym] ()
                   (log/info "Require" (name sym))
                   (require sym)
                   ;; (println "Loading " (name sym))
                   sym
                   ))
            doall))))

(defn nrepl-running?
  "Is the nREPL server running?"
  ([] (nrepl-running? @nrepl-server))
  ([the-server]
     (not (nil? the-server))))

(defn nrepl-port
  "Returns the port number that NREPL is listening to"
  ([] (nrepl-port @nrepl-server))
  ([server] (:port server)))

(defn stop-nrepl!
  "Stops the nREPL server. Returns nil."
  ([] (stop-nrepl! @nrepl-server))
  ([the-server]
    (if (nrepl-running? the-server)
      (nrepl/stop-server the-server))
    (reset! nrepl-server nil)
    nil))

(defn- restart-nrepl!
  "Restarts (stops if necessary, then starts) an nREPL server on the given port, returning the server object.
   Intended to be called by clojure.core/swap!."
  ([the-server] (restart-nrepl! the-server 7888))
  ([the-server port]
    (if (nrepl-running? the-server)
      (stop-nrepl! the-server))
    (log/info "Starting nREPL at port " port)
    (nrepl/start-server :port port :handler cider-nrepl-handler)))

(defn start-nrepl!
  "Starts up an nREPL server, returning the port it's running on."
  ([] (start-nrepl! 7888))
  ([port]
     (swap! nrepl-server restart-nrepl! port)
    port))

;; Other Clojure gobbledygook
;;(gen-class :name    alfresco.interop.ClojureInit
;;           :prefix  "ci-"
;;           :state state
;;           :init init
;;           :methods [[loadNamespaces [] void]
;;                     [setNamespaces [java.util.List] void]
;;                     [getNamespaces [] java.util.List]])

(defn getfield
  [this key]
  (@(.state this) key))

(defn setfield
  [this key value]
  (swap! (.state this) into {key value}))

(defn ci-init []
  "Store our fields as a hash"
  [[] (atom {})])

(defn ci-loadNamespaces
  "Bootstraps instance namespaces"
  [this]
  (load-namespaces (getfield this :namespaces)))

(defn ci-getNamespaces
  [this]
  (getfield this :namespaces))

(defn ci-setNamespaces [this namespaces]
  (setfield this :namespaces namespaces))

(defn ni-init []
  "Store our fields as a hash"
  [[] (atom {})])

(defn ni-startNrepl
  [this]
  (alfresco/start-nrepl! (getfield this :port)))

(defn ni-setPort [this port]
  (setfield this :port port))

(defn ni-getPort
  [this]
  (getfield this :port))

;;(gen-class :name alfresco.interop.NReplInit
;;  :prefix "ni-"
;;  :state state
;;  :init init
;;  :methods [[startNrepl [] void]
;;            [setPort [int] void]
;;            [getPort [] int]])
