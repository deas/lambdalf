(require '[spring.surf.webscript :as w]
         '[clojure.tools.logging :as log]
         '[alfresco.server :as s])

(w/create-webscript
 (fn []
   (if (a/nrepl-running?)
     {:status "Started"
      :port   (a/nrepl-port)}
     {:status "Stopped"})))
