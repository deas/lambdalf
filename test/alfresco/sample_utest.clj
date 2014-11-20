;;
;; Copyright (C) 2011,2012 Carlo Sciolla
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns alfresco.sample_utest
  (:use [clojure.test]
        [midje.sweet]
        ;; [alfresco.test])
        )
  (:require [alfresco.auth :as a]
            [alfresco.nodes :as n]
            ;; [clj-http.client :as http]
            [alfresco.transact :as t]))

(defn first-element [sequence default]
  (if (nil? (seq sequence))
    default
    (first sequence)))

;; (background (before :facts (print "foo")))

;; Just a sample
(facts "about `first-element`"
  (fact "it normally returns the first element"
    (first-element [1 2 3] :default) => 1
    (first-element '(1 2 3) :default) => 1)

  ;; I'm a little unsure how Clojure types map onto the Lisp I'm used to.
  (fact "default value is returned for empty sequences"
    (first-element [] :default) => :default
    (first-element '() :default) => :default
    (first-element nil :default) => :default
    (first-element (filter even? [1 3 5]) :default) => :default))
