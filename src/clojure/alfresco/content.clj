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
 
(ns alfresco.content
  (:require [alfresco.core :as c])
  (:import [org.alfresco.model ContentModel]
           [java.io File ByteArrayInputStream]))

(defn content-service
  []
  (.getContentService (c/alfresco-services)))

(defn- is
  "Retrieves an InputStream of the content for the provided node"
  [node]
  (.getContentInputStream (.getReader (content-service) node ContentModel/PROP_CONTENT)))

;; as seen on
;; https://groups.google.com/group/clojure/browse_thread/thread/e5fb47befe8b9199
;; TODO: make sure we're not breaking utf-8 support
;; TODO: consider alternative forms that return content as streams, strings, etc.
(defn read!
  "Returns a lazy seq of the content of the provided node"
  [node]
  (let [is (is node)]
    (map char (take-while #(not= -1 %) (repeatedly #(.read is))))))

(defn get-writer
  "Returns the ContentWriter for the given node & property (default to cm:content).
   Should not normally be used directly - write! is preferable."
  ([node]          (get-writer node ContentModel/PROP_CONTENT))
  ([node property] (.getWriter (content-service) node property true)))

(defmulti write!
  "Writes content to the given node."
  #(type (first %&)))

(defmethod write! java.io.InputStream
  ([src node]          (.putContent (get-writer node) src))
  ([src node property] (.putContent (get-writer node property) src)))

(defmethod write! java.lang.String
  ([src node]          (write! (ByteArrayInputStream. (.getBytes src "UTF-8")) node))
  ([src node property] (write! (ByteArrayInputStream. (.getBytes src "UTF-8")) node property)))

(defmethod write! java.io.File
  ([src node]          (.putContent (get-writer node) src))
  ([src node property] (.putContent (get-writer node property) src)))
