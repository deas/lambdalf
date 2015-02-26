(ns spring-break.factories
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]))

(defn compiler-load [s]
  (clojure.lang.Compiler/load s))

(defprotocol object-factory
  (new-instance [this s res]))

(def clojure-object-factory
  (reify object-factory
    (new-instance [this s res]
      (with-open [rdr (if (Boolean/valueOf res)
                        (java.io.InputStreamReader. (.openStream (io/resource s)))
                        (java.io.StringReader. s))]
        (log/debug "Load : " s)
        (compiler-load rdr)))))
