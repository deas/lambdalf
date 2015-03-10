(ns spring.surf.webscript
  (:require [clojure.tools.logging :as log]
            [alfresco.core :as c]))

(defn- k2s
  "Returns a map ensuring that keys are all Strings and not clojure keywords"
  [amap]
  (zipmap (map name (keys amap))
          (vals amap)))

(defn- s2k
  "Returns a map ensuring that keys are all clojure keywords and no Strings"
  [amap]
  (zipmap (map keyword (keys amap))
          (vals amap)))

(defn args
  "Fetches arguments from the input map by name"
  [model]
  (s2k (.get model "args")))

(defn req-body-str
  "Returns the HTTP request body as a String"
  [model]
  (-> model (.get "requestbody") (.getContent)))

(defn template-args
  "Fetches all the template arguments from the webscript UrlModel"
  [model]
  (s2k (.getTemplateArgs (.get model "url"))))

(defn merge-jmap-model
  "Updates the view-model with the provided one"
  [model view-model]
  (doto model
    (.putAll (k2s view-model))))

;; Ugh! :()
;; (defn js-ext [name]
;;   (-> (c/getbean "javaScriptProcessor")
;;       ()))

;; We keep it here as a ref for now :)
;; (defmacro let-jmap [vars & forms]
;;   "Set up bindings for the entire map. Yes, that may look dangerous and bad. It
;;  makes sense. Trust me."
;;   `;;  (with-local-vars [var ~vars]
;;    ;; (deref var)
;;   (let [~'m ~vars]
;;        (eval
;;         (list 'let (->> ~vars
;;                         .entrySet
;;                         (map (fn [sym#] [(-> sym# .getKey symbol)
;;                                          (list '.get (symbol "m") (.getKey sym#))]))
;;                         (cons [(symbol "m") '~vars])
;;                         (apply concat)
;;                         vec)
;;               '~(conj forms 'do))
;;         ));;)
;;      ;;)
;;   )

(defmacro create-script
  "Create a script."
  [f]
  `(reify spring.surf.webscript.Script
     (run [~'this ~'model ~'extensions]
       (let [{:keys [~'document ~'space]
              :or [~'document nil ~'space nil]}
             (into {}
                   (->> ~'model
                        .entrySet
                        (map (fn[sym#] [(-> sym# .getKey keyword)
                                        (.getValue sym#)]))))]
         (~f)))))


(defmacro create-webscript
  "Create a webscript."
  [f]
  `(reify spring.surf.webscript.Script
     (run [~'this ~'model ~'extensions]
       (let [{:keys [~'model]}
             (into {}
                   (->> ~'model
                        .entrySet
                        (map (fn[sym#] [(-> sym# .getKey keyword)
                                        (.getValue sym#)]))))]
         (merge-jmap-model ~'model (~f))))))
