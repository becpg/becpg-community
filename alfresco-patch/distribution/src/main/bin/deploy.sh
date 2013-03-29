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
	java -jar  $DEPLOY_ROOT/alfresco-mmt.jar install amps/$1 $SERVER/webapps/alfresco.war -force
}

install_share_amp(){
	echo "deploy $1"
	java -jar  $DEPLOY_ROOT/alfresco-mmt.jar install amps/$1 $SERVER/webapps/share.war -force
}


rm -rf $SERVER/webapps/*.bak

echo "**********************************************************"
echo "Deploy core AMP"
echo "**********************************************************"

rm $SERVER/webapps/alfresco.war
rm -rf $SERVER/webapps/alfresco
cp $SERVER/webapps/alfresco.war.setup $SERVER/webapps/alfresco.war

install_core_amp alfresco-core-patch-*.amp

#echo "**********************************************************"
#echo "Deploy share AMP"
#echo "**********************************************************"

#rm $SERVER/webapps/share.war
#rm -rf $SERVER/webapps/share
#cp $SERVER/webapps/share.war.setup $SERVER/webapps/share.war

#install_share_amp alfresco-share-patch-*.amp

echo "**********************************************************"
echo "Deploy patch "
echo "**********************************************************"

#jar ufv $SERVER/webapps/alfresco.war -C dist/alfresco .
jar ufv $SERVER/webapps/share.war -C dist/share .
