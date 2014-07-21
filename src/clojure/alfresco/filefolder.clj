;
; Copyright (C) 2011,2012 Peter Monks
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

(ns alfresco.filefolder
  (:require [clojure.string :as s]
            [alfresco.core  :as c]
            [alfresco.nodes :as n])
  (:import [org.alfresco.service.cmr.model FileFolderService
                                           FileInfo]
           [org.alfresco.model ContentModel]))

(defn ^FileFolderService file-folder-service
  "The file folder service bean."
  []
  (.getFileFolderService (c/alfresco-services)))

(defn exists?
  "Does the given node exist?"
  [node]
  (.exists (file-folder-service) node))

(defn exist?
  "Do all of the given nodes exist?"
  [nodes]
  (every? true? (map #(exists? %) nodes)))

(defn create!
  "Creates a child node of the specified parent, using standard file/folder
  associations and names. Returns the nodeRef of the newly created node."
  ([parent child-name]
   (.getNodeRef (.create (file-folder-service) parent child-name ContentModel/TYPE_CONTENT)))
  ([parent child-name child-type]
   (.getNodeRef (.create (file-folder-service) parent child-name child-type))))

(defn delete!
  "Deletes the given node.  Note: duplicate of alfresco.nodes/delete!."
  [node]
  { :pre (exists? node)
    :post (not (exists? node)) }
  (.delete (file-folder-service) node))

(defn copy!
  "Copies a node from one place to another. Returns the nodeRef of the newly created node."
  [source-node target-parent new-name]
  { :pre (exist? [source-node target-parent]) }
  (.getNodeRef (.copy (file-folder-service) source-node target-parent new-name)))

; N00B WARNING: Don't yet know how to express this inline as an anonymous fn...
(defn- first-sequential?
  [& args]
  (sequential? (first args)))

(defmulti find-by-path
  "Finds a node at the given path location, optionally providing a start-node (defaults to Company Home).
  Returns nil if the path does not identify a node."
  first-sequential?)

(defmethod find-by-path true
  ([path-elems] (find-by-path path-elems (n/company-home)))
  ([path-elems start-node]
    (let [file-info (.resolveNamePath (file-folder-service) start-node path-elems false)]
      (if (nil? file-info)
        nil
        (.getNodeRef file-info)))))

; This version of the multi-method converts the incoming path to a string, then splits it on "/". Naive, but useful...
(defmethod find-by-path false
  ([path] (find-by-path path (n/company-home)))
  ([path start-node]
    (find-by-path (filter #(< 0 (.length %)) (s/split (str path) #"/")) start-node)))

