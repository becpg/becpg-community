#!/bin/bash

if [ $# -ne 2 ]
   then
      echo "Usage: $0 <server path> <modules (core,saiku)>"
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

if [[ $2 = *core* ]]; then
	echo "**********************************************************"
	echo "Deploy core "
	echo "**********************************************************"
	
	rm $SERVER/webapps/alfresco.war
	rm -rf $SERVER/webapps/alfresco
	cp $SERVER/webapps/alfresco.war.setup $SERVER/webapps/alfresco.war
	
	rm $SERVER/webapps/share.war
	rm -rf $SERVER/webapps/share
	cp $SERVER/webapps/share.war.setup $SERVER/webapps/share.war
	
	 install_core_amp alfresco-core-patch-*.amp
	 install_core_amp becpg-core-*.amp
	 install_core_amp becpg-project-core-*.amp 
	 install_core_amp becpg-designer-core-*.amp
	 install_core_amp becpg-plm-core-*.amp 
	 
	 install_share_amp becpg-share-*.amp
	 install_share_amp becpg-project-share-*.amp 
	 install_share_amp becpg-designer-share-*.amp
	 install_share_amp becpg-plm-share-*.amp 

fi


if [[ $2 = *saiku* ]]; then
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
fi











