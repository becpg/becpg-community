#!/bin/bash

MAVEN_OPTS="-Xms512m -Xmx2G" mvn clean install -Ppurge,run,integration-test
