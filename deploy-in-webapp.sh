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

java -jar  $ALF/bin/alfresco-mmt.jar install becpg-core/target/$BECPG_CORE $SERVER/webapps/alfresco.war -force

echo "**********************************************************"
echo "Deploy share AMP"
echo "**********************************************************"

#becpg-share-amp
rm $SERVER/webapps/share.war
rm -rf $SERVER/webapps/share
cp $SERVER/webapps/share.war.setup $SERVER/webapps/share.war
java -jar  $ALF/bin/alfresco-mmt.jar install becpg-share/target/$BECPG_SHARE $SERVER/webapps/share.war -force


echo "**********************************************************"
echo "Deploy OLAP Cube"
echo "**********************************************************"
cd $BECPG_OLAP_ROOT/target
tar xvfz becpg-olap-*-distribution.tar.gz
cd becpg-olap-*
echo `pwd`
cp -f $BECPG_ROOT/common.sh .
./deploy.sh
cd $BECPG_OLAP_ROOT/target
rm -rf becpg-olap-*