#!/bin/bash

MAVEN_OPTS="-Xms512m -Xmx2G" mvn clean install -Prun,integration-test,purge
#mvn surefire-report:report  -DskipTests=true