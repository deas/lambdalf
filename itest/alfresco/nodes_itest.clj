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

(ns alfresco.nodes_itest
  (:use [midje.sweet]
        ;; [clojure.test]
        [alfresco.itest])
  (:require [clj-http.client :as http]
            [alfresco.auth :as a]
            [alfresco.nodes :as n]
            [alfresco.model :as m]
            ;; [alfresco.transact :as t]
            ))

;; Fixtures do not kick in here
;; (defn init-fixture [f]
;;   (ensure-context)
;;   (f))

;; (use-fixtures :once init-fixture)

(background (before :facts (ensure-context)))

(facts "About nodes" :it
  (fact "Company home node is in place" :it
    (a/as-admin
     (n/property (n/company-home)
                 :cm/name))  => "Company Home")
  (fact "Company home is folder" :it
    (m/qname-keyword
     (a/as-admin
      (n/type-qname (n/company-home)))) => :cm/folder)
  (fact ""))
