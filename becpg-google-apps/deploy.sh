#!/bin/sh

. ../common.sh

if [ $# -ne 1 ]
   then
      echo "Usage: $0 <instance name>"
      exit 0
fi

export SERVER=$INSTANCE_DIR/$1
export DEPLOY_ROOT=$SERVER/../../deploy

echo "**********************************************************"
echo "Build AMP"
echo "**********************************************************"

mvn clean package -Dmaven.test.skip=true $MVN_PROFILE

install_core_amp(){
	echo "deploy $1"
	java -jar  $DEPLOY_ROOT/alfresco-mmt.jar install target/$1 $SERVER/webapps/alfresco.war -force
}
rm -rf $SERVER/webapps/alfresco

install_core_amp becpg-google-apps-*.amp

#clean dir
rm -rf $SERVER/webapps/*.bak

