#!/bin/sh

if [ $# -ne 1 ]
   then
      echo "Usage: $0 <nodeRef>"
      exit 0
fi



export LOCAL_SERVER=http://admin:becpg@localhost:8080/alfresco/service/becpg/remote/entity
export REMOTE_SERVER=http://admin:becpg@tours.aliasource.fr:8080/alfresco/service/becpg/remote/entity

wget -v  --http-user=admin --http-password=becpg  --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us -O entity.xml $REMOTE_SERVER?nodeRef=$1 
curl -v -H "Content-Type: application/xml"  -X POST --data @entity.xml  $LOCAL_SERVER?nodeRef=$1&callback=$REMOTE_SERVER

