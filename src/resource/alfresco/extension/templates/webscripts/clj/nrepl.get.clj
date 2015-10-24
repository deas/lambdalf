(require '[spring.surf.webscript :as w]
         '[clojure.tools.logging :as log]
         '[contentreich.nrepl :as cr-nrepl])

(w/create-webscript
 (fn []
   (if (cr-nrepl/nrepl-running?)
     {:status "Started"
      :port   (cr-nrepl/nrepl-port)}
     {:status "Stopped"})))
