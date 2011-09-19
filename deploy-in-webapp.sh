#!/bin/sh

. ./common.sh

mvn clean package -Dmaven.test.skip=true $MVN_PROFILE

#becpg-amp
rm $SERVER/webapps/alfresco.war
rm -rf $SERVER/webapps/alfresco
rm -rf SERVER/webapps/*.bak
cp $SERVER/webapps/alfresco.war.setup $SERVER/webapps/alfresco.war

java -jar  $ALF/bin/alfresco-mmt.jar install becpg-core/target/$BECPG_CORE $SERVER/webapps/alfresco.war -force

#becpg-share-amp
rm $SERVER/webapps/share.war
rm -rf $SERVER/webapps/share
cp $SERVER/webapps/share.war.setup $SERVER/webapps/share.war
java -jar  $ALF/bin/alfresco-mmt.jar install becpg-share/target/$BECPG_SHARE $SERVER/webapps/share.war -force
