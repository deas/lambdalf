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
  ;; (println (nil? model) " " (nil? view-model) " - " model " - " view-model)
  ;; (log/debug "Merging back into view model")
  (let [view-model-orig (.get model "model")]
    (.putAll view-model-orig (k2s view-model))
    model
    ))

;; Ugh! :()
;; (defn js-ext [name]
;;   (-> (c/getbean "javaScriptProcessor")
;;       ()))

(defmacro let-jmap [vars & forms]
  "Set up bindings for the entire map. Yes, that may look dangerous and bad. It
 makes sense. Trust me."
  `(eval
    (list 'let (->> ~vars
                    .entrySet
                    (map (fn [sym#] [(-> sym# .getKey symbol)
                                     (list '.get (symbol "model") (.getKey sym#))]))
                    (cons [(symbol "model") '~vars])
                    (apply concat)
                    vec)
          '~(conj forms 'do))
    ))

(defmacro create-script
  "Create a (web)script for the processor(s)."
  [f  & options]
  (let [{:keys [webscript] :or {webscript true}} options]
    `(reify spring.surf.webscript.WebScript
       (run [~'this ~'model]
         ;; Wrap bindings
         (let-jmap ~'model
           ~(if webscript
              `(merge-jmap-model ~'model (~f))
              `(~f)))
         ))))
