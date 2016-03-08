#!/bin/bash
# Downloads the spring-loaded lib if not existing and runs the full all-in-one
# (Alfresco + Share + Solr) using the runner project

MAVEN_OPTS="-Xmx1024m" mvn test-compile install -Prun
