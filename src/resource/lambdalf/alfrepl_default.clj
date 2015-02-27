(ns lambdalf.alfrepl-default
       (:require [alfresco.server :as srv]))

(log/info "Initializing")
(srv/start-nrepl! 7889)
;; (srv/add-cp ...)
