#!/bin/sh

if [ $# -ne 1 ]
   then
      echo "Usage: $0 <server path>"
      exit 0
fi

export SERVER=$1
export DEPLOY_ROOT=$SERVER/../../deploy

install_core_amp(){
	echo "deploy $1"
	java -jar  $DEPLOY_ROOT/alfresco-mmt.jar install amps/$1 $SERVER/webapps/alfresco.war -force -nobackup
}

install_share_amp(){
	echo "deploy $1"
	java -jar  $DEPLOY_ROOT/alfresco-mmt.jar install amps/$1 $SERVER/webapps/share.war -force -nobackup
}

read -p "Deploy OpenID? (y/n)" ansopenid 
read -p "Deploy report server? (y/n)" ansreport

echo "**********************************************************"
echo "Deploy core AMP"
echo "**********************************************************"

rm $SERVER/webapps/alfresco.war
rm -rf $SERVER/webapps/alfresco
cp $SERVER/webapps/alfresco.war.setup $SERVER/webapps/alfresco.war

install_core_amp alfresco-core-patch-*.amp
install_core_amp becpg-controls-core-*.amp 
install_core_amp becpg-designer-core-*.amp
install_core_amp becpg-core-*.amp


if [ "$ansopenid" = "y" ]; then
install_core_amp becpg-google-apps-*.amp
fi

echo "**********************************************************"
echo "Deploy share AMP"
echo "**********************************************************"

rm $SERVER/webapps/share.war
rm -rf $SERVER/webapps/share
cp $SERVER/webapps/share.war.setup $SERVER/webapps/share.war

install_share_amp alfresco-share-patch-*.amp
install_share_amp becpg-controls-share-*.amp
install_share_amp becpg-designer-share-*.amp
install_share_amp becpg-share-*.amp

#clean dir
rm -rf $SERVER/webapps/*.bak

echo "**********************************************************"
echo "Deploy patch "
echo "**********************************************************"

jar ufv $SERVER/webapps/share.war -C dist/share .
jar ufv $SERVER/webapps/alfresco.war -C dist/alfresco .
if [ "$ansopenid" = "y" ]; then
jar ufv $SERVER/webapps/share.war -C dist/share_openid .
fi




if [ "$ansreport" = "y" ]; then
echo "**********************************************************"
echo "Deploy Report Server"
echo "**********************************************************"

rm -rf $SERVER/webapps/becpg-report
cp amps/becpg-report-*.war $SERVER/webapps/becpg-report.war
fi
