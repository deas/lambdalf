;;
;; Copyright (C) 2011,2012 Carlo Sciolla
;;               2014      Peter Monks rewritten around fn-generating macros
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

(ns alfresco.behave
  (:require [alfresco.core  :as c]
            [alfresco.model :as m])
  (:import [org.alfresco.repo.policy JavaBehaviour
                                     Behaviour$NotificationFrequency]))

;; Not available from the ServiceRegistry, unfortunately...
(defn policy-component
  []
  (c/get-bean "policyComponent"))

(defmacro ^:private gen-policy-registration-fn
  "Generates a registration function for an Alfresco policy.
  fn-name:       the name of the generated Clojure function
  policy-if:     the Alfresco interface of the selected policy
  policy-method: the Java method in that interface that needs to be implemented by the custom behaviour
  params:        the parameters that method takes"
  [fn-name policy-if policy-method params]
  `(defn ~fn-name
     ~(str "Registers the function f as a handler for behaviour " policy-if
           ", for the given qname. f must accept parameters: "
           "[" (clojure.string/join " " params) "]"
           "\nNote: Handlers only fire on transaction commit.")
     [~'qname ~'f]
     (let [p# (reify ~policy-if
                (~policy-method [~'this ~@params]
                  (~'f ~@params)))
           b# (JavaBehaviour. p#
                              ~(str policy-method)
                              Behaviour$NotificationFrequency/TRANSACTION_COMMIT)]
       (.bindClassBehaviour (policy-component)
                            ~(symbol (str policy-if "/QNAME"))
                            (m/qname ~'qname)
                            b#)
       nil)))

(def ^:private policies
  "A list of all of the Alfresco policies supported by lambdalf (note: doesn't include all of the available policies).
  This list is used by the gen-policy-registration-fns macro to generate the various registration methods and Alfresco
  Java API interop boilerplate."
  [
    ;clojure-fn-name         alfresco-policy-class                                                      policy-method             policy-method-parameters
    ;----------------------- -------------------------------------------------------------------------- ------------------------- --------------------------------
    ; NodeServicePolicies
    ['on-create-node!        'org.alfresco.repo.node.NodeServicePolicies$OnCreateNodePolicy             'onCreateNode             '[child-assoc-ref]]
    ['on-update-node!        'org.alfresco.repo.node.NodeServicePolicies$OnUpdateNodePolicy             'onUpdateNode             '[node]]
    ['on-move-node!          'org.alfresco.repo.node.NodeServicePolicies$OnMoveNodePolicy               'onMoveNode               '[old-child-assoc-ref new-child-assoc-ref]]
    ['on-update-properties!  'org.alfresco.repo.node.NodeServicePolicies$OnUpdatePropertiesPolicy       'onUpdateProperties       '[node before-props after-props]]
    ['on-delete-node!        'org.alfresco.repo.node.NodeServicePolicies$OnDeleteNodePolicy             'onDeleteNode             '[child-assoc-ref is-node-archived?]]
    ['on-restore-node!       'org.alfresco.repo.node.NodeServicePolicies$OnRestoreNodePolicy            'onRestoreNode            '[child-assoc-ref]]
    ['on-set-node-type!      'org.alfresco.repo.node.NodeServicePolicies$OnSetNodeTypePolicy            'onSetNodeType            '[node old-type new-type]]
    ['on-add-aspect!         'org.alfresco.repo.node.NodeServicePolicies$OnAddAspectPolicy              'onAddAspect              '[node aspect-qname]]
    ['on-remove-aspect!      'org.alfresco.repo.node.NodeServicePolicies$OnRemoveAspectPolicy           'onRemoveAspect           '[node aspect-qname]]
    ['on-create-child-assoc! 'org.alfresco.repo.node.NodeServicePolicies$OnCreateChildAssociationPolicy 'onCreateChildAssociation '[child-assoc-ref is-new-node?]]
    ['on-delete-child-assoc! 'org.alfresco.repo.node.NodeServicePolicies$OnDeleteChildAssociationPolicy 'onDeleteChildAssociation '[child-assoc-ref]]
    ['on-create-assoc!       'org.alfresco.repo.node.NodeServicePolicies$OnCreateAssociationPolicy      'onCreateAssociation      '[node-assoc-ref]]
    ['on-delete-assoc!       'org.alfresco.repo.node.NodeServicePolicies$OnDeleteAssociationPolicy      'onDeleteAssociation      '[node-assoc-ref]]

    ; ContentServicePolicies
    ['on-content-update!          'org.alfresco.repo.content.ContentServicePolicies$OnContentUpdatePolicy         'onContentUpdate         '[node is-new-content?]]
    ['on-content-property-update! 'org.alfresco.repo.content.ContentServicePolicies$OnContentPropertyUpdatePolicy 'onContentPropertyUpdate '[node property-qname before-value after-value]]
    ['on-content-read!            'org.alfresco.repo.content.ContentServicePolicies$OnContentReadPolicy           'onContentRead           '[node]]

    ; VersionServicePolicies
    ['on-create-version! 'org.alfresco.repo.version.VersionServicePolicies$OnCreateVersionPolicy 'onCreateVersion '[class-ref-qname node version-properties node-details]]

    ; CopyServicePolicies
    ['on-copy-complete! 'org.alfresco.repo.copy.CopyServicePolicies$OnCopyCompletePolicy 'onCopyComplete '[class-ref-qname source-node target-node is-copy-to-new-node? copy-map]]

    ; CheckOutCheckInServicePolicies
    ['on-check-out!        'org.alfresco.repo.coci.CheckOutCheckInServicePolicies$OnCheckOut       'onCheckOut       '[working-copy-node]]
    ['on-check-in!         'org.alfresco.repo.coci.CheckOutCheckInServicePolicies$OnCheckIn        'onCheckIn        '[node]]
    ['on-cancel-check-out! 'org.alfresco.repo.coci.CheckOutCheckInServicePolicies$OnCancelCheckOut 'onCancelCheckOut '[node]]
  ])

(defn- third
  "The third item in the given collection."
  [col]
  (first (next (next col))))

(defn- fourth
  "The fourth item in the given collection."
  [col]
  (first (next (next (next col)))))

(defmacro ^:private gen-policy-registration-fns
  "Generates all policy registration fns defined in the 'policies' vector."
  []
  `(do ~@(for [policy policies]
           `(gen-policy-registration-fn ~(first  policy)
                                        ~(second policy)
                                        ~(third  policy)
                                        ~(fourth policy)))))

(gen-policy-registration-fns)
