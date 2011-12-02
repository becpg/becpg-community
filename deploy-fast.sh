#!/bin/sh

. ./common.sh

#becpg-amp

#cp -rf becpg-core/src/main/webapp/* $SERVER/webapps/alfresco/
cp -rf becpg-core/src/main/resources/* $SERVER/webapps/alfresco/WEB-INF/classes

#becpg-share
cp -rf becpg-share/src/main/web/* $SERVER/webapps/share/
cp -rf becpg-share/src/main/config/* $SERVER/webapps/share/WEB-INF/classes/alfresco

