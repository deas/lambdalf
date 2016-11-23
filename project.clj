;;
;; Copyright © 2011-2014 Carlo Sciolla
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
;;
;; Contributors:
;;    Carlo Sciolla   - initial implementation
;;    Peter Monks     - contributor
;;    Andreas Steffan - contributor

;; Nice idea, but does not play with lein ancient
;;
;; (def alfresco-version "5.1.e")
;; (def alfresco-core-version "5.6")
;; (def spring-version "3.2.14.RELEASE")
;; (def spring-surf-version "5.8")
;; (def h2-version "1.4.190")
;; (def xml-apis-version-override "1.4.01")
;; (def junit-version-override "4.11")
;; (def cider-nrepl-version "0.11.0")
;; (def refactor-nrepl-version "1.2.0")
;; (def jetty-version "9.2.11.v20150529")
;; (def gorilla-repl-version "0.3.5-SNAPSHOT")
;; (def websocket-version "1.0")


(defproject org.clojars.deas/lambdalf "1.9.999"
  :title "lambdalf"
  :description "Lambdalf -- Clojure support for Alfresco, batteries included"
  :url "https://github.com/lambdalf/lambdalf"
  :license {:name "Apache License, Version 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :min-lein-version "2.4.0"
  :repositories [["alfresco.public" "https://artifacts.alfresco.com/nexus/content/groups/public/"]]
  ;; To keep things simple for now, we put all the tooling here as well
  ;; http://jakemccrary.com/blog/2015/01/11/overview-of-my-leiningen-profiles-dot-clj/
  ;; parinfer/0.1.0-SNAPSHOT/parinfer-0.1.0-SNAPSHOT
  :dependencies [[org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.namespace "0.2.10"]
                 [org.clojure/tools.trace "0.7.9"]
                 [org.clojure/clojure "1.9.0-alpha13"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 ;; [com.gfredericks/debug-repl "0.0.7"]
                 ;; [spyscope "0.1.5"]
                 [evalive "1.1.0"]
                 [org.clojure/java.classpath "0.2.3"]
                 [org.clojure/java.jmx "0.3.1"]
                 ;; schmetterling introduces deps conflicting with alfresco
                 ;; [schmetterling "0.0.8"]
                 ;; SNAPSHOTS do not build uberjars?
                 [cider/cider-nrepl "0.14.0" #_~cider-nrepl-version] ; 8.0-SNAPSHOT"]
                 [refactor-nrepl "1.2.0" #_~refactor-nrepl-version]
                 ;; WARNING: do _not_ add test, provided or runtime dependencies
                 ;; here as they will be included in the uberjar, regardless of scope.
                 ;; See https://github.com/technomancy/leiningen/issues/741 for an
                 ;; explanation of why this occurs.
                 [com.stuartsierra/component "0.3.1"]

                 ;; Weeding out deps here!
                 [ring/ring-core "1.4.0" :exclusions [ring/ring-codec
                                                      commons-fileupload
                                                      crypto-random
                                                      clj-time
                                                      commons-io]]
                 [clj-time "0.9.0" :exclusions [joda-time]]
                 [ring/ring-codec "1.0.0" :exclusions [commons-codec]]
                 [crypto-random "1.2.0" :exclusions [commons-codec]]
                 [compojure "1.4.0"
                  :exclusions [ring/ring-core]]
                 [org.clojars.deas/gorilla-repl-ng "0.3.6-SNAPSHOT"
                  :exclusions [*/*]
                  #_[http-kit org.slf4j/slf4j-api
                     javax.servlet/servlet-api
                     grimradical/clj-semver
                     ch.qos.logback/logback-classic
                     org.clojure/data.codec
                     org.clojure/tools.logging]]            ;; A ton of deps :exclusions [com.sun.jdmk/jmxtools]
                 [ring/ring-servlet "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [cheshire "5.5.0" :exclusions [*/*]]
                 ;; for gorilla websocket-relay
                 ;; jackson-core "2.5.3", alfresco jackson-core-2.3.2
                 [com.fasterxml.jackson.dataformat/jackson-dataformat-smile "2.3.2"
                  :exclusions [*/*]]
                 [com.fasterxml.jackson.dataformat/jackson-dataformat-cbor "2.3.2"
                  :exclusions [*/*]]
                 [org.clojars.deas/gorilla-plot "0.2.0-SNAPSHOT"]
                 [clojail "1.0.6"]
                 ;; TODO Untangle those two in gorilla-repl
                 [http-kit "2.2.0"]
                 [grimradical/clj-semver "0.3.0"
                  :exclusions [org.clojure/clojure]]
                 [ring-cors "0.1.8"]
                 [ring-middleware-format "0.7.0"]]
  :aot [gorilla-repl.servlet]
  ;; :aot [contentreich.ring-servlet]
  :javac-options ["-target" "1.7" "-source" "1.7"]
  :plugins [[cider/cider-nrepl "0.14.0"]
            [refactor-nrepl "1.2.0" #_~refactor-nrepl-version]
            ;; http://dev.clojure.org/jira/browse/NREPL-53
            ;; [com.gfredericks/nrepl-53-monkeypatch "0.1.0"]
            ;; [lein-cljsbuild "1.1.0"]
            ;; [lein-figwheel "0.4.0"]
            ]
  :repl-options {
                 :timeout          120000
                 :nrepl-middleware [;; com.gfredericks.debug-repl/wrap-debug-repl
                                    ;;  [cider.nrepl.middleware.classpath/wrap-classpath
                                    ;;   cider.nrepl.middleware.complete/wrap-complete
                                    ;;   cider.nrepl.middleware.info/wrap-info
                                    ;;   cider.nrepl.middleware.inspect/wrap-inspect
                                    ;;   cider.nrepl.middleware.macroexpand/wrap-macroexpand
                                    ;;   cider.nrepl.middleware.stacktrace/wrap-stacktrace
                                    ;;   cider.nrepl.middleware.test/wrap-test
                                    ;;   cider.nrepl.middleware.trace/wrap-trace
                                    ;;   cider.nrepl.middleware.undef/wrap-undef]
                                    ]
                 ;; :init-ns scratch
                 ;; :init (create-application-context)
                 }

  :profiles {:dev      {:plugins      [
                                       ;; [lein-midje "3.1.3"]
                                       ]                    ;;[lein-amp "0.3.0"]]}
                        :source-paths ["src/clojure"]       ;; "dev"
                        :dependencies [
                                       [org.alfresco/alfresco-repository "5.1.e"
                                        :classifier "h2scripts"
                                        :exclusions [
                                                     commons-codec
                                                     ;; commons-fileupload
                                                     ]]
                                       ;; [spyscope "0.1.5"]
                                       [com.h2database/h2 "1.4.190" #_~h2-version]
                                       [clj-http "2.1.0"
                                        :exclusions [
                                                     commons-codec
                                                     ]
                                        ]
                                       [org.eclipse.jetty/jetty-server "9.2.11.v20150529"]
                                       [org.eclipse.jetty.websocket/websocket-server "9.2.11.v20150529"]
                                       ;; [midje "1.8.3"]
                                       [org.eclipse.jetty/jetty-webapp "9.2.11.v20150529"]
                                       [org.eclipse.jetty/jetty-util "9.2.11.v20150529"]
                                       ]}
             ;;             :uberjar  { :aot :all }
             :test     {:dependencies [
                                       [org.alfresco/alfresco-repository "5.1.e"
                                        :classifier "h2scripts"
                                        :exclusions [
                                                     ;; commons-codec
                                                     ;; commons-fileupload
                                                     ]]
                                       [com.h2database/h2 "1.4.190" #_~h2-version]
                                       [clj-http "2.1.0"]
                                       [org.eclipse.jetty/jetty-server "9.2.11.v20150529"]
                                       [org.eclipse.jetty.websocket/websocket-server "9.2.11.v20150529"]
                                       [org.eclipse.jetty/jetty-webapp "9.2.11.v20150529"]
                                       [org.eclipse.jetty/jetty-util "9.2.11.v20150529"]
                                       [junit/junit "4.11"]]}
             :provided {:dependencies [
                                       ;; [org.clojure/clojurescript "1.7.122"]
                                       ;; We need to fiddle around with the deps so the clojure deps chain shows up
                                       [org.alfresco/alfresco-core "5.6"
                                        :exclusions [
                                                     ;; commons-codec
                                                     ;; joda-time
                                                     ]]
                                       [org.alfresco/alfresco-data-model "5.1.e"
                                        ;; :exclusions [org.apache.tika/tika-parsers]
                                        ]
                                       [org.alfresco/alfresco-mbeans "5.1.e"]
                                       [org.alfresco/alfresco-remote-api "5.1.e"]
                                       [org.alfresco/alfresco-repository "5.1.e"
                                        :exclusions [commons-collections
                                                     ;; commons-codec
                                                     ;; joda-time
                                                     ;; commons-fileupload
                                                     ]]

                                       ;; You have to build the web-client yourself for now
                                       ;; "mvn -f pom-alfresco-web-client.xml install" in web-client/
                                       ;; [org.alfresco/alfresco-web-client                      "5.1.e"#_~alfresco.version]
                                       [org.springframework/spring-context "3.2.14.RELEASE"]
                                       [org.springframework/spring-beans "3.2.14.RELEASE"]
                                       [org.springframework.extensions.surf/spring-webscripts "5.8"
                                        :exclusions [
                                                     ;; commons-fileupload
                                                     ]]
                                       ;; The deps alfresco really wants
                                       ;; override wrong dep from core causing RTE when starting up the context

                                       [commons-collections "3.2.2"]
                                       ;;  Weeding out fileupload, joda, commons-io does not work - comes in via clj(!) deps
                                       [commons-fileupload "1.3.1"]
                                       [joda-time "2.8.1"]
                                       [commons-io "2.4"]
                                       [commons-codec "1.10"]
                                       [javax.websocket/javax.websocket-api "1.0"]
                                       [javax.servlet/javax.servlet-api "3.1.0"]
                                       [xml-apis/xml-apis "1.4.01"]]}}
  ;; :aot               [alfresco]
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :resource-paths ["src/resource"]
  ;; :amp-source-path   "src/amp"
  ;; :amp-target-war    [org.alfresco/alfresco "5.1.e"#_~alfresco.version :extension "war"
  :javac-target "1.7"
  ;; Beware !!! refactor-nrepl middleware can kick off midje integration tests!
  :test-paths ["itest" "test"]                              ;; ["itest" "test"]

  ;; :injections [(require 'spyscope.core)]
  ;; http://www.jayway.com/2014/09/09/integration-testing-setup-with-midje-and-leiningen/
  :aliases {"itest" ["midje" ":filters" "it"]               ;;"src/clojure" "test" "itest"
            "test"  ["midje"]
            "utest" ["midje" ":filters" "-it"]}
  )
