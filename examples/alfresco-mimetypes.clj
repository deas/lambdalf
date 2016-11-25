;; gorilla-repl.fileformat = 1

;; **
;;; # Wecome to Gorilla. In Alfresco. In Production.
;;; 
;;; Credits for the foundation got to Jony Hudson (gorilla-repl) and Carlo Sciolla (lambdalf)
;;; and all the other "lower layer people" for making this possible.
;;; 
;;; 
;;; 
;; **

;; @@
(ns contentreich-scratch
  (:require [gorilla-plot.core :as plot]
            [alfresco.nodes :as n]
            [alfresco.transact :as tx]
            [alfresco.auth :as auth])
  (:use [clojure.pprint]))
;; @@

;; @@
;; Only data retrieval here is Alfresco specific. Everything else is generic.
;; Traversing the repo, filtering content (weeding out folders), counting hits
;; per mime-type. Could have used search for cm:content instead.

(def mimetype-data (tx/in-ro-tx-as
 (auth/admin)
 (->> (n/company-home)
      n/to-seq
      (filter #(some? (n/mime-type %)))
      (group-by #(n/mime-type %))
      (map (fn[x] [(first x) (count (second x))]))
      (apply map list))))
;; @@

;; @@
(def plot-1 (plot/bar-chart (first mimetype-data) (second mimetype-data) :plot-size 800))
;; @@

;; @@
;; A bit messy on the axis

plot-1
;; @@

;; @@
(pprint plot-1)
;; @@

;; @@
;; Fixing the fix

(def plot-2 (-> plot-1
           (assoc-in [:content :axes 0 :properties :labels :angle :value] 90)
           (assoc-in [:content :axes 0 :properties :labels :align :value] "right")))
;; @@

;; @@
;; Better

plot-2
;; @@
