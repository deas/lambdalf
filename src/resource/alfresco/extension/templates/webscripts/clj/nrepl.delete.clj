(require '[spring.surf.webscript :as w]
         '[clojure.tools.logging :as log]
         '[alfresco.server :as s])

(w/create-webscript
 (fn []
   (s/stop-nrepl!)
   (w/return model {:status "stopped"})))
