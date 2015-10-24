# Lambdalf -- Clojure support for Alfresco

**WARNING**: This may not be the fork you are looking for.

<img src="https://raw.githubusercontent.com/lambdalf/lambdalf/gh-pages/images/logo-small.png"
 alt="Lambdalf logo" title="Two logos at the price of one!" align="right" />

This library adds [Clojure](http://www.clojure.org/) support to the open source
[Alfresco Content Management System](http://www.alfresco.com/). Specifically, it:

 * adds an idiomatic Clojure wrapper around (an increasing %age of) the [Alfresco Java API](http://wiki.alfresco.com/wiki/Java_Foundation_API)
 * provides support for implementing Alfresco extension points in Clojure, including
   [behaviours](https://github.com/pmonks/lambdalf/blob/master/src/clojure/alfresco/behave.clj), and
   [web scripts](https://github.com/lambdalf/lambdalf/blob/master/src/clojure/spring/surf/webscript.clj)
 * adds an nREPL server to the Alfresco server (disabled by default - requires administrator rights to enable),
   allowing for productive REPL-style experimentation and development within Alfresco
 * packages all of this, along with the Clojure runtime, into a **JAR** that 3rd party code can depend on (thereby avoiding
  conflicts between different Clojure extensions). This fork does **not** use AMP in any way.

## Packaging
It is this **JAR** artifact that should be deployed to a running
Alfresco server, prior to the deployment of your own JAR or AMP if you want to use the latter.

## Installing lambdalf into Alfresco
Run `lein uberjar` and drop that file in `alfresco/WEB-INF/lib`.

### Opening a REPL
For security reasons (i.e. **it opens a massive script injection attack hole!**) the nREPL server included in lambdalf is
not running by default. To enable it (**keeping in mind that it opens a massive script injection attack hole!**) an
administrator-only HTTP POST Web Script is provided at `/alfresco/service/clojure/nrepl`. For a default installation
of Alfresco on localhost, you can run:

```shell
    $ curl -u admin:admin -X POST http://localhost:8080/alfresco/service/clojure/nrepl
```

to enable the nREPL server.  The Web Script's JSON response includes the port that the nREPL server is running on
(default is 7888).  From there you can use leiningen's built-in nREPL client to connect to the nREPL server:

```shell
    $ lein repl :connect 7888
```

See below for some example Clojure expressions that can validate the installation (although being able to connect is
itself a good sign that everything's hunky dory).

To disable the nREPL server, you may issue an HTTP DELETE to the same Web Script:

```shell
    $ curl -u admin:admin -X DELETE http://localhost:8080/alfresco/service/clojure/nrepl
```

You may also query the status of the nREPL server via an HTTP GET:

```shell
    $ curl -u admin:admin http://localhost:8080/alfresco/service/clojure/nrepl
```

## Developing with lambdalf

lambdalf is (NOT YET!) available as a Maven artifact from [Clojars](https://clojars.org/org.clojars.lambdalf/lambdalf).
Plonk the following in your project.clj :plugins, `lein deps` and you should be good to go:

```clojure
[org.clojars.lambdalf/lambdalf "#.#.#"]
```

Here's some sample code from an nREPL session connected to a running Alfresco repository. Note that there are
[better ways](https://github.com/lambdalf/lambdalf/blob/master/src/clojure/alfresco/nodes.clj#L65) to get a handle to the Company
Home `nodeRef`.  Note also that unlike Alfresco's native Java API, each `ResultSet` is automatically closed after a search.

```
    user> (require '[alfresco.auth :as a])
    nil
    user> (require '[alfresco.search :as s])
    nil
    user> (require '[alfresco.nodes :as n])
    nil
    user> (def company-home
              (a/as-admin
               (first
                (s/query "PATH:\"/*\" AND TYPE:\"cm:folder\""))))
    #'user/company-home
    user> (clojure.pprint/pprint
       (a/as-admin
        (n/properties company-home)))
    {"sys:store-identifier" "SpacesStore",
     "cm:modifier" "System",
     "cm:title" "Company Home",
     "cm:description" "The company root space",
     "sys:store-protocol" "workspace",
     "app:icon" "space-icon-default",
     "sys:node-dbid" 13,
     "cm:created" #<Date Sun Sep 04 15:11:18 CEST 2011>,
     "sys:node-uuid" "43356014-0428-4e86-9490-e78a6c0c48ef",
     "cm:modified" #<Date Sun Sep 04 15:11:18 CEST 2011>,
     "cm:name" "Company Home",
     "cm:creator" "System"}
    nil
```

## Developer Information

[GitHub project](https://github.com/lambdalf/lambdalf)

[Bug Tracker](https://github.com/lambdalf/lambdalf/issues)

## License

Copyright © 2011-2014 Carlo Sciolla

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
