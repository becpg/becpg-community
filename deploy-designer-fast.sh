#!/bin/sh

. ./common.sh

if [ $# -ne 1 ]
   then
      echo "Usage: $0 <instance name>"
      exit 0
fi


export SERVER=$INSTANCE_DIR/$1

#becpg-amp

#cp -rf becpg-core/src/main/webapp/* $SERVER/webapps/alfresco/
cp -rf becpg-core/src/main/resources/* $SERVER/webapps/alfresco/WEB-INF/classes

#wget --delete-after --http-user=admin --http-password=becpg --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us --post-data reset=on http://localhost:8080/alfresco/service/index

#becpg-share
cp -rf becpg-share/src/main/web/* $SERVER/webapps/share/
cp -rf becpg-share/src/main/config/* $SERVER/webapps/share/WEB-INF/classes/alfresco/
cp -rf becpg-designer/becpg-designer-share/src/main/web/* $SERVER/webapps/share/
cp -rf becpg-designer/becpg-designer-share/src/main/config/* $SERVER/webapps/share/WEB-INF/classes/alfresco/

wget --delete-after --http-user=admin --http-password=becpg --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us --post-data reset=on http://localhost:8080/share/page/index
