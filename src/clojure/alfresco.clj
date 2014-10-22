;
; Copyright (C) 2011,2012 Carlo Sciolla, Peter Monks
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns alfresco
  (:require [clojure.tools.nrepl.server :as nrepl]
            [clojure.tools.logging :as log]
            [cider.nrepl :refer (cider-nrepl-handler)]))

; Hold a reference to the NREPL server
(def ^:private nrepl-server (atom nil))

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

; Other Clojure gobbledygook
(gen-class :name    alfresco.interop.ClojureInit
           :prefix  "ci-"
           :methods [[setNamespaces [java.util.List] void]])

(defn load-ns
  "loads a given ns provided its dotted String representation"
  [^String ns]
  (let [s (.replaceAll ns "\\." "/")]
    (load (str "/" s))))

(defn ci-setNamespaces
  "Bootstraps the given namespaces"
  [this ns-list]
  (apply load-ns ns-list))

(defn ni-startNrepl
  [this]
  (alfresco/start-nrepl!))

(gen-class :name alfresco.interop.NReplInit
  :prefix "ni-"
  ;; :implements [org.springframework.beans.factory.InitializingBean]
  :methods [[startNrepl [] void]])
