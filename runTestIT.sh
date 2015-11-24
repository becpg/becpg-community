#!/bin/bash

mvn clean -P purge
MAVEN_OPTS="-Xms512m -Xmx2G" mvn install -Prun,integration-test
