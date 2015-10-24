(require '[spring.surf.webscript :as w]
         '[clojure.tools.logging :as log]
         '[contentreich.nrepl :as nrepl])

(w/create-webscript
 (fn []
   {:status "started"
    :port   (nrepl/start-nrepl!)}))
