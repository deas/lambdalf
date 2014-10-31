(ns spring-break.factories
  (:require [clojure.tools.logging :as log]))

(defn compiler-load [s]
  (log/debug "Load " s)
  (clojure.lang.Compiler/load
   (java.io.StringReader. s)))

(defprotocol object-factory
  (new-instance [this s]))

(def clojure-object-factory
  (reify object-factory
    (new-instance [this s]
      (compiler-load s))))
