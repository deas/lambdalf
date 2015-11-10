(ns contentreich.gorilla-repl
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware [keyword-params :as keyword-params]
             [params :as params]
             [json :as json]]
            [cheshire.core :as cc]
            [clojure.tools.nrepl.server :as nrepl-server]
            [gorilla-repl.render-values-mw :as render-mw]   ;; it's essential this import comes after the previous one! It
    ;; refers directly to a var in nrepl (as a hack to workaround
    ;; a weakness in nREPL's middleware resolution).
            [cider.nrepl :as cider]
            [gorilla-repl.renderer :as renderer] ;; this is needed to bring the render implementations into scope
            [ring.util.response :as res]
            [clojure.tools.nrepl :as nrepl]
            [clojure.tools.nrepl [transport :as transport]
             [server :as server]]
            [gorilla-repl.files :as files]
            [clojure.tools.logging :as log]
            [clojure.pprint :as pp]))

;; a wrapper for JSON API calls
(defn wrap-api-handler
  [handler]
  (-> handler
      (keyword-params/wrap-keyword-params)
      (params/wrap-params)
      (json/wrap-json-response)))

;; the worksheet load handler
(defn load-worksheet
  [req]
  ;; TODO: S'pose some error handling here wouldn't be such a bad thing
  (when-let [ws-file (:worksheet-filename (:params req))]
    (let [_ (print (str "Loading: " ws-file " ... "))
          ws-data (slurp (str ws-file) :encoding "UTF-8")
          _ (println "done.")]
      (res/response {:worksheet-data ws-data}))))


;; the client can post a request to have the worksheet saved, handled by the following
(defn save
  [req]
  ;; TODO: error handling!
  (when-let [ws-data (:worksheet-data (:params req))]
    (when-let [ws-file (:worksheet-filename (:params req))]
      (print (str "Saving: " ws-file " ... "))
      (spit ws-file ws-data)
      (println (str "done. [" (java.util.Date.) "]"))
      (res/response {:status "ok"}))))


;; More ugly atom usage to support defroutes
(def excludes (atom #{".git"}))
;; API endpoint for getting the list of worksheets in the project
(defn gorilla-files [req]
  (let [excludes @excludes]
    (res/response {:files (files/gorilla-filepaths-in-current-directory excludes)})))

;; configuration information that will be made available to the webapp
(def conf (atom {}))
(defn set-config [k v] (swap! conf assoc k v))
;; API endpoint for getting webapp configuration information
(defn config [req] (res/response @conf))


;; the combined routes - we serve up everything in the "public" directory of resources under "/".
;; The REPL traffic is handled in the websocket-transport ns.
(defroutes app-routes
           (GET "/alfresco/lambdalf/foo" [] "Hello Alfresco Foo")
           (GET "/alfresco/lambdalf/gorilla-repl/load" [] (wrap-api-handler load-worksheet))
           (POST "/alfresco/lambdalf/gorilla-repl/save" [] (wrap-api-handler save))
           (GET "/alfresco/lambdalf/gorilla-repl/gorilla-files" [] (wrap-api-handler gorilla-files))
           (GET "/alfresco/lambdalf/gorilla-repl/config" [] (wrap-api-handler config))
           ;; (GET "/repl" [] ws-relay/ring-handler)
           (route/resources "/alfresco/lambdalf/gorilla-repl/")
           (route/files "/alfresco/lambdalf/gorilla-repl/project-files" [:root "."]))

(defn get-routes [] app-routes)

(comment defn drawbridge-ring-handler
         "Returns a Ring handler implementing an HTTP transport endpoint for nREPL.
          The handler will work when routed onto any URI.  Note that this handler
          requires the following standard Ring middleware to function properly:
            * keyword-params
            * nested-params
            * wrap-params
          a.k.a. the Compojure \"api\" stack.
          nREPL messages should be encoded into POST request parameters; messages
          are only accepted from POST parameters.
          A GET or POST request will respond with any nREPL response messages cached
          since the last request.  If:
            * the handler is created with a non-zero :default-read-timeout, or
            * a session's first request to the handler specifies a non-zero
              timeout via a REPL-Response-Timeout header
          ...then each request will wait the specified number of milliseconds for
          additional nREPL responses before finalizing the response.
          All response bodies have an application/json Content-Type, consisting of
          a map in the case of an error, or an array of nREPL response messages
          otherwise.  These messages are output one per line (/ht CouchDB), like so:
          [
          {\"ns\":\"user\",\"value\":\"3\",\"session\":\"d525e5..\"}
          {\"status\":[\"done\"],\"session\":\"d525e5..\"}
          ]
          A custom nREPL handler may be specified when creating the handler via
          :nrepl-handler.  The default
          (via `(clojure.tools.nrepl.server/default-handler)`) is appropriate
          for textual REPL interactions, and includes support for interruptable
          evaluation, sessions, readably-printed evaluation values, and
          prompting for *in* input.  Please refer to the main nREPL documentation
          for details on semantics and message schemas for these middlewares."
         [& {:keys [nrepl-handler default-read-timeout cookie-name]
             :or   {nrepl-handler        (server/default-handler)
                    default-read-timeout 0
                    cookie-name          "drawbridge-session"}}]
         ;; TODO heartbeat for continuous feeding mode
         (-> (fn [{:keys [params session headers request-method] :as request}]
               ;(println params session)
               (let [msg (clojure.walk/keywordize-keys params)]
                 (cond
                   (not (#{:post :get} request-method)) illegal-method-error

                   (and (:op msg) (not= :post request-method)) message-post-error

                   :else
                   (let [[read write :as transport] (or (::transport session)
                                                        (transport/piped-transports))
                         client (or (::client session)
                                    (nrepl/client read (if-let [timeout (get headers response-timeout-header*)]
                                                         (Long/parseLong timeout)
                                                         default-read-timeout)))]
                     (response transport client
                               (do
                                 (when (:op msg)
                                   (future (server/handle* msg nrepl-handler write)))
                                 (client)))))))
             (memory-session :cookie-name cookie-name)))

(defn- response
  [transport client response-seq]
  (doall (map cc/generate-string response-seq)))

(def ^:private nrepl-handler (apply server/default-handler
                                    (-> (map resolve cider/cider-middleware)
                                        (conj #'render-mw/render-values))))

(defn process-message
  "..."
  [msg store & {:keys [nrepl-handler read-timeout]
                :or   {nrepl-handler (server/default-handler)
                       read-timeout  1000}}]                ;; Long/MAX_VALUE -> Not what we want
  ;; TODO heartbeat for continuous feeding mode
  (log/info "Got handler" nrepl-handler)
  (let [[read write :as transport] (or (::transport store)
                                       (do (.put store ::transport (transport/piped-transports))
                                           (::transport store)))
        client (nrepl/client read read-timeout)]
    (log/debug "Processing message " (with-out-str (pp/pprint msg) " response timeout = " read-timeout))
    (response transport client
              (do
                (when (:op msg)
                  (future (server/handle* msg nrepl-handler write)))
                (client)))))

(defn process-json-message
  [data store]
  (let [m (assoc (cc/parse-string data true) :as-html 1)]
    (-> m
        (process-message store :nrepl-handler nrepl-handler))))
