(ns contentreich.utils
  (:require [clojure.tools.logging :as log])
  (:import [org.springframework.core.io.support PathMatchingResourcePatternResolver]))


;; Implement one sans Alfresco
;; (defn create-application-context [])

;; "classpath*:alfresco/**/*-context.xml"
(defn cp-find-resources
  "Find spring Resources in by location pattern"
  [loc-pattern]
  (. (PathMatchingResourcePatternResolver.) getResources loc-pattern))

;; WARNING: Note that "classpath*:" when combined with Ant-style patterns will only work reliably with at
;; least one root directory before the pattern starts, unless the actual target files reside in the file
;; system. This means that a pattern like "classpath*:*.xml" will not retrieve files from the root of jar
;; files but rather only from the root of expanded directories. This originates from a limitation in the
;; JDK's ClassLoader.getResources() method which only returns file system locations for a passed-in empty
;; String (indicating potential roots to search).

;; (map #(.. % (getURL) (toString)) (contentreich.utils/cp-find-resources "classpath*:alfresco/**/*-context.xml"))
