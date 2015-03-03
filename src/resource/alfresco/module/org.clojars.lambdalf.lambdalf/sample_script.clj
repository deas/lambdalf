(require '[spring.surf.webscript :as w]
         '[clojure.tools.logging :as log])

(w/create-script
 (fn [model]
   (log/info "Works!")
   ;; (.log logger "Yo")
   {"more" "stuff"})) ;; // No keywords for java
