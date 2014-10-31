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

(ns alfresco.nodes_test
  (:use [clojure.test]
        ;; [clojure.tools.nrepl]
        [alfresco.test])
  (:require [clj-http.client :as http]
            [alfresco.auth :as a]
            [alfresco.model :as m]
            [alfresco.nodes :as n]))

(defn init-fixture [f]
  (ensure-context)
  ;;(ensure-nrepl)
  ;;(let [c (client (connect :port 7888) 1000)]
  ;;  (message c {:op :eval :code (code (require '[alfresco.auth :as a]))})
  ;;  (message c {:op :eval :code (code (require '[alfresco.nodes :as n]))})
  ;;  (message c {:op :eval :code (code (require '[alfresco.model :as m]))})
  (f))

(use-fixtures :once init-fixture)

;; (deftest a-test
;;   (testing "FIXME, I fail."
;;     (is (= 0 1))))

;;(defftest company-home-ftests
;;  (do
;;    (are [result f expr] (= result (f (repl-eval client (code expr))))
;;
;;         ;; Get the node name
;;         "Company Home" repl-value (a/as-admin
;;                                    (n/property (n/company-home)
;;                                                :cm/name))
;;         ;; Get the node type
;;         :cm/folder repl-value (m/qname-keyword
;;                                (a/as-admin
;;                                 (n/type-qname (n/company-home)))))))

;; Will move to itests
(deftest company-home-itests
  (testing "Company home failed"
    (is (= "Company Home" (a/as-admin
                          (n/property (n/company-home)
                                      :cm/name)))))
  (testing "Company home type failed"
    (is (= :cm/folder (m/qname-keyword
                       (a/as-admin
                        (n/type-qname (n/company-home))))))))
