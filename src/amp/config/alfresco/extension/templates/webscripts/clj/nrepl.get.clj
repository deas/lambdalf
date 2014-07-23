(ns lambdalf.webscript.nrepl
  (:require [spring.surf.webscript :as w]
            [alfresco :as a])
  (:import [spring.surf.webscript WebScript]))

(deftype NreplStatusWebScript
  []
  WebScript
  (run [this in out model]
    (w/return model (if (a/nrepl-running?)

                      ; started
                      {:status "Started"
                       :port   (a/nrepl-port)}

                      ; not started
                      {:status "Stopped"}))))

(NreplStatusWebScript.)
