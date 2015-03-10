(require '[spring.surf.webscript :as w]
         '[clojure.tools.logging :as log])

(w/create-script
 (fn []
   (log/info "Works! Document : " document)
   ;; return value goes to nirvana for basic scripts
 :webscript false))
