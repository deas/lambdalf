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

(ns alfresco.search
  (:require [alfresco.core :as c]
            [alfresco.auth :as a])

  (:import [org.alfresco.service.cmr.repository StoreRef]
           [org.alfresco.service.cmr.search SearchService
                                            SearchParameters
                                            SearchParameters$Operator
                                            LimitBy
                                            QueryConsistency]))

(defn ^SearchService search-service
  []
  (.getSearchService (c/alfresco-services)))

(comment defn camel-to-dash
  [s]
  (apply str (map #(if (Character/isUpperCase %)
                    (str "-" (lower-case %))
                    %)
                  s)))

(defn query
  "Bummer, use the source!"
  [query & args]
  (let [params
        (let [{:keys [bulkfetch-enabled default-fieldname store default-fts-op default-op
                      exclude-tenant-filter language limit limit-by max-items max-permission-checks
                      max-permission-check-time-millis max-raw-resultset-size-for-in-memory-sort
                      skip-count search-term query-consistency use-in-memory-sort spell-check]
               :or {bulkfetch-enabled true
                    default-fieldname "TEXT"
                    store StoreRef/STORE_REF_WORKSPACE_SPACESSTORE
                    default-fts-op SearchParameters$Operator/OR
                    default-op SearchParameters$Operator/OR
                    exclude-tenant-filter false
                    language SearchService/LANGUAGE_LUCENE
                    limit 500
                    limit-by LimitBy/UNLIMITED
                    max-items -1
                    max-permission-checks 1000
                    max-permission-check-time-millis 10000
                    max-raw-resultset-size-for-in-memory-sort nil
                    skip-count 0
                    search-term nil
                    query-consistency QueryConsistency/DEFAULT
                    use-in-memory-sort nil
                    spell-check false
                    }} args]
          (doto (SearchParameters.)
            (.addStore store)
            ;; FIXME !
            ;; (.addFacetQuery)
            ;; (.addFieldFacet)
            ;; (.addExtraParameter)
            ;; (.addLocale)
            ;; (.addQueryParameterDefinition)
            ;; (.addQueryTemplate)
            ;; (.addSort)
            ;; (.addTextAttribute)
            (.setQuery query)
            (.setDefaultFTSOperator default-fts-op)
            (.setDefaultOperator default-op)
            (.setDefaultFieldName default-fieldname)
            (.setBulkFetchEnabled bulkfetch-enabled)
            (.setExcludeTenantFilter exclude-tenant-filter)
            (.setLanguage language)
            (.setLimit limit)
            (.setLimitBy limit-by)
            (.setMaxItems max-items)
            (.setMaxPermissionChecks max-permission-checks)
            (.setMaxRawResultSetSizeForInMemorySort max-raw-resultset-size-for-in-memory-sort)
            ;; (.setMlAnalaysisMode)
            (.setMaxPermissionCheckTimeMillis max-permission-check-time-millis)
            (.setQueryConsistency query-consistency)
            (.setSearchTerm search-term)
            (.setSkipCount skip-count)
            (.setSpellCheck false)))]
    (with-open [rs (.query (search-service) params)]
      (.getNodeRefs rs))))
