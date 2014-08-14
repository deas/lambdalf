;
; Copyright © 2011-2014 Carlo Sciolla
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
;
; Contributors:
;    Carlo Sciolla - initial implementation
;    Peter Monks   - contributor

(def alfresco-version "5.0.a")

(defproject org.clojars.lambdalf/lambdalf "1.9.999"
  :title            "lambdalf"
  :description      "Lambdalf -- Clojure support for Alfresco"
  :url              "https://github.com/lambdalf/lambdalf"
  :license          { :name "Apache License, Version 2.0"
                      :url "http://www.apache.org/licenses/LICENSE-2.0" }
  :min-lein-version "2.4.0"
  :repositories [
                  ["alfresco.public" "https://artifacts.alfresco.com/nexus/content/groups/public/"]
                ]
  :dependencies [
                  [org.clojure/clojure     "1.6.0"]
                  [org.clojure/tools.nrepl "0.2.3"]
		  [cider/cider-nrepl "0.7.0"]
                  ; WARNING: do _not_ add test, provided or runtime dependencies here as they will be included in the uberjar,
                  ; regardless of scope.  See https://github.com/technomancy/leiningen/issues/741 for an explanation of why
                  ; this occurs.
                ]
  :repl-options {:nrepl-middleware
                 [cider.nrepl.middleware.classpath/wrap-classpath
                  cider.nrepl.middleware.complete/wrap-complete
                  cider.nrepl.middleware.info/wrap-info
                  cider.nrepl.middleware.inspect/wrap-inspect
                  cider.nrepl.middleware.macroexpand/wrap-macroexpand
                  cider.nrepl.middleware.stacktrace/wrap-stacktrace
                  cider.nrepl.middleware.test/wrap-test
                  cider.nrepl.middleware.trace/wrap-trace
                  cider.nrepl.middleware.undef/wrap-undef]}

  :profiles {:dev      { :plugins [;;[lein-amp "0.3.0"]
                                   ] }
             :uberjar  { :aot :all }
             :test     { :dependencies [
                                         [clj-http                       "0.9.2"           :scope "test"]
                                         [tk.skuro.alfresco/h2-support   "1.6"             :scope "test"]
                                         [com.h2database/h2              "1.3.174"         :scope "test"]
                                         [org.eclipse.jetty/jetty-runner "9.2.1.v20140609" :scope "test"]
                                       ] }
             :provided { :dependencies [
                                         [org.alfresco/alfresco-core                            ~alfresco-version :scope "provided"]
                                         [org.alfresco/alfresco-data-model                      ~alfresco-version :scope "provided"]
                                         [org.alfresco/alfresco-mbeans                          ~alfresco-version :scope "provided"]
                                         [org.alfresco/alfresco-remote-api                      ~alfresco-version :scope "provided"]
                                         [org.alfresco/alfresco-repository                      ~alfresco-version :scope "provided"]
                                         [org.springframework/spring-context                    "3.0.5.RELEASE"  :scope "provided"]
                                         [org.springframework/spring-beans                      "3.0.5.RELEASE"  :scope "provided"]
                                         [org.springframework.extensions.surf/spring-webscripts "1.2.0"          :scope "provided"]
                                       ] }
            }
  :source-paths      ["src/clojure"]
  :java-source-paths ["src/java"]
  :resource-paths    ["src/resource"]
;;  :amp-source-path   "src/amp"
;;  :amp-target-war    [org.alfresco/alfresco ~alfresco-version :extension "war"]
  :javac-target      "1.7"
  )
