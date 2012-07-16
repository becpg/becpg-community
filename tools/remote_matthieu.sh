#!/bin/sh

export LOCAL_SERVER=http://localhost:8080/alfresco/service/becpg/remote/entity
export LOCAL_USER=admin
export LOCAL_PASSWORD=becpg
export REMOTE_SERVER=http://tours.aliasource.fr:8080/alfresco/service/becpg/remote/entity
export REMOTE_USER=admin
export REMOTE_PASSWORD=becpg

if [ $# -ne 1 ]
   then
     
		#LIST
		wget --quiet --http-user=$REMOTE_USER --http-password=$REMOTE_PASSWORD  --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us -O list.xml $REMOTE_SERVER/list 
		
		count=1
		
		while [ -n "$nodeRef" -o $count = 1 ]
		do
		   nodeRef=`cat list.xml | xpath -q -e //*[$count]/@nodeRef | sed s/nodeRef=//g |sed s/\"//g`
		    
		   echo "\nGetting $nodeRef"; 
			wget --quiet --http-user=$REMOTE_USER --http-password=$REMOTE_PASSWORD  --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us -O entity.xml $REMOTE_SERVER?nodeRef=$nodeRef
			curl --user $LOCAL_USER:$LOCAL_PASSWORD --silent -H "Content-Type: application/xml"  -X PUT --data @entity.xml  $LOCAL_SERVER?callback=$REMOTE_SERVER&callbackUser=$REMOTE_USER&callbackPassword=$REMOTE_PASSWORD > nodeRef
			
			export entityNodeRef=`cat nodeRef`
			
			# DATA
			wget --quiet  --http-user=$REMOTE_USER --http-password=$REMOTE_PASSWORD  --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us -O data.xml $REMOTE_SERVER/data?nodeRef=$nodeRef
			curl --user $LOCAL_USER:$LOCAL_PASSWORD --silent -H "Content-Type: application/xml"  -X POST --data @data.xml  $LOCAL_SERVER/data?nodeRef=$entityNodeRef
			 
		   count=$((count+1))
		done

      exit 0
fi


wget --http-user=$REMOTE_USER --http-password=$REMOTE_PASSWORD  --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us -O entity.xml $REMOTE_SERVER?nodeRef=$1 
curl --user $LOCAL_USER:$LOCAL_PASSWORD -H "Content-Type: application/xml"  -X PUT --data @entity.xml  $LOCAL_SERVER?callback=$REMOTE_SERVER&callbackUser=$REMOTE_USER&callbackPassword=$REMOTE_PASSWORD > nodeRef


# DATA
wget  --http-user=$REMOTE_USER --http-password=$REMOTE_PASSWORD  --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us -O data.xml $REMOTE_SERVER/data?nodeRef=$1 
curl --user $LOCAL_USER:$LOCAL_PASSWORD -H "Content-Type: application/xml"  -X POST --data @data.xml  $LOCAL_SERVER/data?nodeRef=`cat nodeRef` > nodeRef
