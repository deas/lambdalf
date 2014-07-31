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
  (:require [clojure.string :as s]
            [alfresco.core  :as c]
            [alfresco.model :as m])
  (:import [org.alfresco.repo.policy JavaBehaviour
                                     Behaviour$NotificationFrequency]))

;; no static String to use here, unfortunately
(defn policy-component
  []
  (c/get-bean "policyComponent"))

(comment
(defn- bind-class-behaviour!
  [policy-qname binding-type-qname behaviour]
  (.bindClassBehaviour (policy-component)
                       policy-qname
                       (m/qname binding-type-qname)
                       behaviour)
  nil)
)

(def ^:private policies
  [
    ['on-add-aspect!  org.alfresco.repo.node.NodeServicePolicies$OnAddAspectPolicy  'onAddAspect  '[node qname]]
    ['on-create-node! org.alfresco.repo.node.NodeServicePolicies$OnCreateNodePolicy 'onCreateNode '[child-assoc-ref]]
  ])

(defmacro ^:private gen-behaviour-registration-fn
  [fn-name policy-if policy-method params]
;  `(def ~fn-name
  `(defn ~fn-name
     ~(str "Registers the function f as a handler for behaviour " policy-if
           ", for the given qname. f must accept " (count params)
           " parameters: " (clojure.string/join ", " params)
           "\nNote: Handlers only fire on transaction commit.")
;     (fn [~'qname ~'f]
     [~'qname ~'f]
     (let [p# (reify ~policy-if
                (~policy-method [~'this ~@params]
                  (~'f [~@params])))
           b# (JavaBehaviour. p#
                              ~(str policy-method)
                              Behaviour$NotificationFrequency/TRANSACTION_COMMIT)]
       (.bindClassBehaviour (policy-component)
                            ~(symbol (str policy-if "/QNAME"))
                            (m/qname ~'qname)
                            b#))));)

(gen-behaviour-registration-fn on-add-aspect!  org.alfresco.repo.node.NodeServicePolicies$OnAddAspectPolicy  onAddAspect  [node qname])
(gen-behaviour-registration-fn on-create-node! org.alfresco.repo.node.NodeServicePolicies$OnCreateNodePolicy onCreateNode [child-assoc-ref])

(comment
(defmacro ^:private gen-behaviour-registration-fns
  []
  `(do ~@(for [policy policies]
           (let [fn-name#          (first policy)
                 interface#        (first (next policy))
                 interface-method# (first (next (next policy)))
                 args#             (first (next (next (next policy))))]
             `(println fn-name# interface# interface-method# args#)))))
;           `(gen-behaviour-registration-fn ~fn-name#
;                                           ~interface#
;                                           ~interface-method#
;                                           ~args#)))))

(gen-behaviour-registration-fns)
)






;####TODO: consider adding 3-arity version that receives a notification frequency (perhaps as a keyword?)
(comment
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
)

;####TODO: consider adding 3-arity version that receives a notification frequency (perhaps as a keyword?)
(comment
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
)

; ####TODO: add fns for other policy types
; OR (better)
; generate the functions somewhat like https://github.com/xsc/jansi-clj/blob/master/src/jansi_clj/core.clj#L104
