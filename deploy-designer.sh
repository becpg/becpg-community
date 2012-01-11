#!/bin/sh

. ./common.sh

echo "**********************************************************"
echo "Build AMP"
echo "**********************************************************"

mvn clean package -Dmaven.test.skip=true $MVN_PROFILE


echo "**********************************************************"
echo "Deploy core AMP"
echo "**********************************************************"

#becpg-amp
rm $SERVER/webapps/alfresco.war
rm -rf $SERVER/webapps/alfresco
rm -rf $SERVER/webapps/*.bak
cp $SERVER/webapps/alfresco.war.setup $SERVER/webapps/alfresco.war
echo "deploy becpg-controls/becpg-controls-core/target/becpg-controls-core-$BECPG_VERSION.amp"
java -jar  $ALF/bin/alfresco-mmt.jar install becpg-controls/becpg-controls-core/target/becpg-controls-core-$BECPG_VERSION.amp $SERVER/webapps/alfresco.war -force
echo "deploy becpg-designer/becpg-designer-core/target/becpg-designer-core-$BECPG_VERSION.amp"
java -jar  $ALF/bin/alfresco-mmt.jar install becpg-designer/becpg-designer-core/target/becpg-designer-core-$BECPG_VERSION.amp $SERVER/webapps/alfresco.war -force


echo "**********************************************************"
echo "Deploy share AMP"
echo "**********************************************************"

#becpg-share-amp
rm $SERVER/webapps/share.war
rm -rf $SERVER/webapps/share
cp $SERVER/webapps/share.war.setup $SERVER/webapps/share.war
echo "deploy  becpg-controls/becpg-controls-share/target/becpg-controls-share-$BECPG_VERSION.amp"
java -jar  $ALF/bin/alfresco-mmt.jar install becpg-controls/becpg-controls-share/target/becpg-controls-share-$BECPG_VERSION.amp $SERVER/webapps/share.war -force
echo "deploy  becpg-designer/becpg-designer-share/target/becpg-designer-share-$BECPG_VERSION.amp"
java -jar  $ALF/bin/alfresco-mmt.jar install becpg-designer/becpg-designer-share/target/becpg-designer-share-$BECPG_VERSION.amp $SERVER/webapps/share.war -force


