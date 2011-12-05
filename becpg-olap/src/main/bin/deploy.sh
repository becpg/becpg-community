#!/bin/bash
. ./common.sh

echo "**********************************************************"
echo "Deploy OLAP Cube"
echo "**********************************************************"
rm $SERVER/webapps/saiku.war
rm $SERVER/webapps/saiku-ui.war
rm -rf $SERVER/webapps/saiku
rm -rf $SERVER/webapps/saiku-ui
cp $SERVER/webapps/saiku.war.setup $SERVER/webapps/saiku.war
cp $SERVER/webapps/saiku-ui.war.setup $SERVER/webapps/saiku-ui.war

jar ufv $SERVER/webapps/saiku.war -C dist/saiku .
jar ufv $SERVER/webapps/saiku-ui.war -C dist/saiku-ui .