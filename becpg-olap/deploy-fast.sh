#!/bin/sh

. ../common.sh

cp -rf _diff/* $SERVER/webapps
cp -rf conf/* $SERVER/webapps/saiku/WEB-INF/classes/saiku-datasources
mkdir -p  $SERVER/webapps/saiku/WEB-INF/classes/becpg
cp -rf schemas/* $SERVER/webapps/saiku/WEB-INF/classes/becpg/

