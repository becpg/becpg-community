#!/bin/bash
# Downloads the spring-loaded lib if not existing and runs the full all-in-one
# (Alfresco + Share + Solr) using the runner project

MAVEN_OPTS="-Xms512m -Xmx2G" mvn test-compile install -Prun -DskipTests=true
