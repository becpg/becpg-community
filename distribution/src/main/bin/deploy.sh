#!/bin/sh

. ../common.sh

if [ $# -ne 1 ]
   then
      echo "Usage: $0 <instance>"
      exit 0
fi



export SERVER=$INSTANCE_DIR/$1
export DEPLOY_ROOT=$TC_DIR/deploy

cd amps


#becpg-amp
rm $SERVER/webapps/alfresco.war
rm -rf $SERVER/webapps/alfresco
rm -rf $SERVER/webapps/*.bak
cp $SERVER/webapps/alfresco.war.setup $SERVER/webapps/alfresco.war

echo "**********************************************************"
echo "Deploy core AMP"
echo "**********************************************************"


echo "deploy becpg-controls-core-$BECPG_VERSION.amp"
java -jar  $DEPLOY_ROOT/alfresco-mmt.jar install becpg-controls-core-$BECPG_VERSION.amp $SERVER/webapps/alfresco.war -force
echo "deploy becpg-designer-core-$BECPG_VERSION.amp"
java -jar  $DEPLOY_ROOT/alfresco-mmt.jar install becpg-designer-core-$BECPG_VERSION.amp $SERVER/webapps/alfresco.war -force
echo "deploy becpg-core/target/becpg-core-$BECPG_VERSION.amp"
java -jar  $DEPLOY_ROOT/alfresco-mmt.jar install becpg-core-$BECPG_VERSION.amp $SERVER/webapps/alfresco.war -force

echo "**********************************************************"
echo "Deploy share AMP"
echo "**********************************************************"

#becpg-share-amp
rm $SERVER/webapps/share.war
rm -rf $SERVER/webapps/share
cp $SERVER/webapps/share.war.setup $SERVER/webapps/share.war
echo "deploy  becpg-controls-share-$BECPG_VERSION.amp"
java -jar  $DEPLOY_ROOT/alfresco-mmt.jar install becpg-controls-share-$BECPG_VERSION.amp $SERVER/webapps/share.war -force
echo "deploy  becpg-designer-share-$BECPG_VERSION.amp"
java -jar  $DEPLOY_ROOT/alfresco-mmt.jar install becpg-designer-share-$BECPG_VERSION.amp $SERVER/webapps/share.war -force
echo "deploy becpg-share-$BECPG_VERSION.amp"
java -jar  $DEPLOY_ROOT/alfresco-mmt.jar install becpg-share-$BECPG_VERSION.amp $SERVER/webapps/share.war -force


read -p "Deploy report server? (y/n)" ans 
if [ "$ans" = "y" ]; then
echo "**********************************************************"
echo "Deploy Report Server"
echo "**********************************************************"

rm -rf $SERVER/webapps/becpg-report
cp becpg-report-$BECPG_VERSION.war $SERVER/webapps/becpg-report.war
fi
