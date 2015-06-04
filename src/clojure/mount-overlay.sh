#!/bin/sh
BASE=/opt/alfresco-5.0/tomcat/webapps/alfresco/WEB-INF/classes
TOP=/home/deas/work/projects/3rd-party/lambdalf/src/clojure

mount -vt aufs -o br="${BASE}":"${TOP}" none "${BASE}"

