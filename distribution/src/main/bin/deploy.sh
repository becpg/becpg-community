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
	java -jar  $DEPLOY_ROOT/alfresco-mmt.jar install $1 $SERVER/webapps/alfresco.war -force -nobackup 
}

install_share_amp(){
	echo "deploy $1"
	java -jar  $DEPLOY_ROOT/alfresco-mmt.jar install $1 $SERVER/webapps/share.war -force -nobackup
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
	
	 cd amps
	 for ampfile in `ls -rt *.amp` 
	 do
		if [[ $ampfile = *-share* ]]; then
	 	   install_share_amp $ampfile
	 	 else
	      install_core_amp $ampfile
		 fi
	 done
	 
	
	 for warfile in `ls *.war`
	 do
		 rm -rf $SERVER/webapps/${warfile%%.*}
		 cp -L $warfile $SERVER/webapps/${warfile%-*}.war
	 done
	 cd ..
	
	 echo "Deploy patch"
	 if test -d "dist/share" ; then
	 	jar ufv $SERVER/webapps/share.war -C dist/share .
	 fi
	 if test -d "dist/alfresco" ; then
	   jar ufv $SERVER/webapps/alfresco.war -C dist/alfresco .
	 fi
	
	
	 #Clean dir 
	 rm -rf $SERVER/temp/*
	 rm -rf $SERVER/work/*
	 rm -rf $SERVER/webapps/*.bak
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











