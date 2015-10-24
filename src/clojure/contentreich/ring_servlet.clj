(ns contentreich.ring-servlet
  (:use compojure.core)
  (:require [ring.util.servlet :as servlet]
            [clojure.tools.logging :as log])
  (:import (java.io PrintWriter)
           (javax.servlet ServletConfig)
           (javax.servlet.http HttpServletRequest HttpServletResponse))
  (:gen-class :extends javax.servlet.http.HttpServlet
              :exposes-methods {init superInit} ))

;; Gives NPE w/o super
;;
;;(defn -init
;;  ([this]
;;   (log/debug "Servlet initialized with no params") (.superInit)
;;  ([this ^ServletConfig config]
;;   (log/debug "Servlet initialized with servlet config" config)(.superInit config))
;;  )


;; context-path/base-path/...

(defn default-handler [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Set a decent handler"})

(def ring-service (atom (servlet/make-service-method default-handler)))

;;
;; (servlet/set-handler (gorilla/get-routes))
(defn set-handler [handler]
  "You want to call this during intialization.
  Try (servlet/set-handler (gorilla/get-routes))"
  (log/info "Creating new service method from handler")
  (reset! ring-service (servlet/make-service-method handler)))

(defn -service
  [this ^HttpServletRequest request ^HttpServletResponse response]
  (@ring-service this request response))

;; (defn -destroy
;;  [this]
;;  (log/debug "Servlet destroyed"))

