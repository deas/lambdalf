(require '[spring.surf.webscript :as w]
         '[clojure.tools.logging :as log]
         '[contentreich.nrepl :as nrepl])

(w/create-webscript
 (fn []
   (nrepl/stop-nrepl!)
   (w/return model {:status "stopped"})))
