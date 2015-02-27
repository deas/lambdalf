(ns jetty
  "Jetty 9 server, shamelessly stolen (from jetty9 ring adapter) and hacked.

  Fire it up like so:

  (def jetty-component
       (jetty/new-jetty (jetty/create-jetty (jetty/create-handler \"./test-webapp\" \"/alfresco\")
                                       {:port 1234 :configurator jetty/alfresco-configure })))
  (.start jetty-component)
  "

  (:import (org.eclipse.jetty.server Handler
                                     Server
                                     Request
                                     ServerConnector
                                     HttpConfiguration
                                     HttpConnectionFactory
                                     SslConnectionFactory
                                     ConnectionFactory)
           (org.eclipse.jetty.server.handler HandlerCollection
                                             AbstractHandler
                                             ContextHandler
                                             HandlerList)
           (org.eclipse.jetty.webapp WebAppContext)
           (org.eclipse.jetty.util.thread QueuedThreadPool
                                          ScheduledExecutorScheduler)
           (org.eclipse.jetty.util.ssl SslContextFactory))
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(defn- http-config
  [{:as options
    :keys [ssl-port secure-scheme output-buffer-size request-header-size
           response-header-size send-server-version? send-date-header?
           header-cache-size]
    :or {ssl-port 443
         secure-scheme "https"
         output-buffer-size 32768
         request-header-size 8192
         response-header-size 8192
         send-server-version? true
         send-date-header? false
         header-cache-size 512}}]
  "Creates jetty http configurator"
  (doto (HttpConfiguration.)
    (.setSecureScheme secure-scheme)
    (.setSecurePort ssl-port)
    (.setOutputBufferSize output-buffer-size)
    (.setRequestHeaderSize request-header-size)
    (.setResponseHeaderSize response-header-size)
    (.setSendServerVersion send-server-version?)
    (.setSendDateHeader send-date-header?)
    (.setHeaderCacheSize header-cache-size)))

(defn- ssl-context-factory
  "Creates a new SslContextFactory instance from a map of options."
  [{:as options
    :keys [keystore keystore-type key-password client-auth
           truststore trust-password truststore-type]}]
  (let [context (SslContextFactory.)]
    (if (string? keystore)
      (.setKeyStorePath context keystore)
      (.setKeyStore context ^java.security.KeyStore keystore))
    (.setKeyStorePassword context key-password)
    (when keystore-type
      (.setKeyStoreType context keystore-type))
    (when truststore
      (.setTrustStore context ^java.security.KeyStore truststore))
    (when trust-password
      (.setTrustStorePassword context trust-password))
    (when truststore-type
      (.setTrustStoreType context truststore-type))
    (case client-auth
      :need (.setNeedClientAuth context true)
      :want (.setWantClientAuth context true)
      nil)
    context))

(defn- create-server
  "Construct a Jetty Server instance."
  [{:as options
    :keys [port max-threads daemon? max-idle-time host ssl? ssl-port]
    :or {port 80
         max-threads 50
         daemon? false
         max-idle-time 200000
         ssl? false}}]
  (let [pool (doto (QueuedThreadPool. (int max-threads))
               (.setDaemon daemon?))
        server (doto (Server. pool)
                 (.addBean (ScheduledExecutorScheduler.)))

        http-configuration (http-config options)
        http-connector (doto (ServerConnector.
                              ^Server server
                              (into-array ConnectionFactory [(HttpConnectionFactory. http-configuration)]))
                         (.setPort port)
                         (.setHost host)
                         (.setIdleTimeout max-idle-time))

        https-connector (when (or ssl? ssl-port)
                          (doto (ServerConnector.
                                 ^Server server
                                 (ssl-context-factory options)
                                 (into-array ConnectionFactory [(HttpConnectionFactory. http-configuration)]))
                            (.setPort ssl-port)
                            (.setHost host)
                            (.setIdleTimeout max-idle-time)))

        connectors (if https-connector
                     [http-connector https-connector]
                     [http-connector])
        connectors (into-array connectors)]
    (.setConnectors server connectors)
    server))


(defn create-login-configurator []
  (doto (org.eclipse.jetty.security.HashLoginService.)
    (.setName "Repository")
    (.setConfig "realm.properties")
    (.setRefreshInterval (int 0))
    ;; (.putUser "alfresco" (org.eclipse.jetty.util.security.Password. "alfresco"))
    ))

(defn alfresco-configure [server]
  (.addBean server (create-login-configurator)))

(defn create-handler [path ctx-path]
  (let [handler  (WebAppContext. path ctx-path)]
    ;; (doto handler
    ;;   ;; Y U no kick in?
    ;;   (.setExtraClasspath "./webapp/WEB-INF/lib/alfresco-web-client.jar")) ;; path1;path2
    handler))

(defn ^Server create-jetty
  "Start a Jetty webserver to serve the given handler according to the
supplied options:

:port - the port to listen on (defaults to 80)
:host - the hostname to listen on
:join? - blocks the thread until server ends (defaults to true)
:daemon? - use daemon threads (defaults to false)
:ssl? - allow connections over HTTPS
:ssl-port - the SSL port to listen on (defaults to 443, implies :ssl?)
:keystore - the keystore to use for SSL connections
:keystore-type - the format of keystore
:key-password - the password to the keystore
:truststore - a truststore to use for SSL connections
:truststore-type - the format of trust store
:trust-password - the password to the truststore
:max-threads - the maximum number of threads to use (default 50)
:max-idle-time  - the maximum idle time in milliseconds for a connection (default 200000)
:ws-max-idle-time  - the maximum idle time in milliseconds for a websocket connection (default 500000)
:client-auth - SSL client certificate authenticate, may be set to :need, :want or :none (defaults to :none)
:websockets - a map from context path to a map of handler fns:

 {\"/context\" {:on-connect #(create-fn %)                ; ^Session ws-session
                :on-text   #(text-fn % %2 %3 %4)         ; ^Session ws-session message
                :on-bytes  #(binary-fn % %2 %3 %4 %5 %6) ; ^Session ws-session payload offset len
                :on-close  #(close-fn % %2 %3 %4)        ; ^Session ws-session statusCode reason
                :on-error  #(error-fn % %2 %3)}}         ; ^Session ws-session e"
  [handler {:as options
            :keys [max-threads websockets configurator];;  join?
            :or {max-threads 50}}] ;; join? true
  (let [^Server s (create-server options)
        ^QueuedThreadPool p (QueuedThreadPool. (int max-threads))
        ;; ring-app-handler (proxy-handler handler)
        ;; ws-handlers (map (fn [[context-path handler]]
        ;;                    (doto (ContextHandler.)
        ;;                      (.setContextPath context-path)
        ;;                      (.setHandler (proxy-ws-handler handler options))))
        ;;                  websockets)
        contexts (doto (HandlerList.)
                   (.setHandlers
                    ;; (into-array Handler (reverse (conj ws-handlers ring-app-handler)))
                    (into-array Handler [handler])))]
    (.setHandler s contexts)
    (when-let [c configurator]
      (c s))
    ;; (.start s)
    ;; (when join?
    ;;   (.join s))
    s))

(defrecord Jetty [server]
  component/Lifecycle

  (start [component]
    (log/info "Starting Jetty")
    (assoc component :server server)
    (.start server))

  (stop [component]
    (log/info "Stopping Jetty")
    (.stop (:server component))
    (assoc component :server nil))
  )

(defn new-jetty [server]
  (map->Jetty {:server server}))
