#!/bin/bash

MAVEN_OPTS="-Xms512m -Xmx2G" mvn clean verify -Prun,integration-test,purge
#mvn surefire-report:report  -DskipTests=true