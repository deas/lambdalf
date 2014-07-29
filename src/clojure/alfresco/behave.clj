;
; Copyright (C) 2011,2012 Carlo Sciolla
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
; 
;     http://www.apache.org/licenses/LICENSE-2.0
;  
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
 
(ns alfresco.behave
  (:require [alfresco.core  :as c]
            [alfresco.model :as m])
  (:import [org.alfresco.repo.policy JavaBehaviour
                                     Behaviour$NotificationFrequency]
           [org.alfresco.repo.node NodeServicePolicies$OnCreateNodePolicy
                                   NodeServicePolicies$OnAddAspectPolicy]))

;; no static String to use here, unfortunately
(defn policy-component
  []
  (c/get-bean "policyComponent"))

(defn- bind-class-behaviour!
  [policy-qname binding-type-qname behaviour]
  (.bindClassBehaviour (policy-component)
                       policy-qname
                       (m/qname binding-type-qname)
                       behaviour)
  nil)

;####TODO: consider adding 3-arity version that receives a notification frequency (perhaps as a keyword?)
(defn on-add-aspect!
  "Registers the given function f as the handler for the onAddAspect policy of the given QName.
   The function f must accept 2 parameters:
   1. NodeRef
   2. QName"
  [qname f]
  (let [p (reify NodeServicePolicies$OnAddAspectPolicy
            (onAddAspect [this node-in qname-in]
              (f node-in (m/qname-str qname-in))))
        b (JavaBehaviour. p
                          "onAddAspect"
                          Behaviour$NotificationFrequency/TRANSACTION_COMMIT)]
    (bind-class-behaviour! NodeServicePolicies$OnAddAspectPolicy/QNAME qname b)))

;####TODO: consider adding 3-arity version that receives a notification frequency (perhaps as a keyword?)
(defn on-create-node!
  "Registers the given function f as the handler for the onCreateNode policy of the given QName.
   The function f must accept 1 parameter:
   1. ChildAssociationRef"
   [qname f]
   (let [p (reify NodeServicePolicies$OnCreateNodePolicy
             (onCreateNode [this child-assoc-ref]
               (f child-assoc-ref)))
         b (JavaBehaviour. p "onCreateNode" Behaviour$NotificationFrequency/TRANSACTION_COMMIT)]
    (bind-class-behaviour! NodeServicePolicies$OnCreateNodePolicy/QNAME qname b)))

; ####TODO: add fns for other policy types
; OR (better)
; generate the functions somewhat like https://github.com/xsc/jansi-clj/blob/master/src/jansi_clj/core.clj#L104
