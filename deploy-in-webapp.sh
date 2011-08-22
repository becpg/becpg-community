#!/bin/sh

. ./common.sh

#becpg-amp
rm $SERVER/webapps/alfresco.war
rm -rf $SERVER/webapps/alfresco
cp $SERVER/webapps/alfresco.war.setup $SERVER/webapps/alfresco.war
cd becpg-core
mvn clean package -Dmaven.test.skip=true
java -jar  $ALF/bin/alfresco-mmt.jar install target/$BECPG_CORE $SERVER/webapps/alfresco.war -force

#becpg-share-amp
rm $SERVER/webapps/share.war
rm -rf $SERVER/webapps/share
cp $SERVER/webapps/share.war.setup $SERVER/webapps/share.war
cd ../becpg-share
mvn clean package -Dmaven.test.skip=true
java -jar  $ALF/bin/alfresco-mmt.jar install target/$BECPG_SHARE $SERVER/webapps/share.war -force
