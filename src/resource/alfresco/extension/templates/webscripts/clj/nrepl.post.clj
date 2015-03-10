(require '[spring.surf.webscript :as w]
         '[clojure.tools.logging :as log]
         '[alfresco.server :as s])

(w/create-webscript
 (fn []
   {:status "started"
    :port   (a/start-nrepl!)}))
