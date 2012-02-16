#!/bin/sh

. ./common.sh

cd alfresco-patch
mvn clean install -Dmaven.test.skip=true
