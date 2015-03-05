(require '[spring.surf.webscript :as w]
         '[clojure.tools.logging :as log])

(w/create-script
 (fn [model]
   (log/info "Works!")
   (.get model "document")
   ;; // No keywords for java, value goes to nirvana for basic scripts
   {"more" "stuff"}
 :webscript false))
