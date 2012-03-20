#!/bin/sh

export LOCAL_SERVER=http://admin:becpg@localhost:8080/alfresco/service/becpg/remote/entity
export REMOTE_SERVER=http://82.237.72.111:8080/alfresco/service/becpg/remote/entity
export USER=admin
export PASSWORD=becpg

if [ $# -ne 1 ]
   then
     
		#LIST
		wget --quiet --http-user=admin --http-password=becpg  --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us -O list.xml $REMOTE_SERVER/list 
		
		count=1
		
		while [ -n "$nodeRef" -o $count = 1 ]
		do
		   nodeRef=`cat list.xml | xpath -q -e //*[$count]/@nodeRef | sed s/nodeRef=//g |sed s/\"//g`
		    
		   echo "\nGetting $nodeRef"; 
			wget --quiet --http-user=$USER --http-password=$PASSWORD  --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us -O entity.xml $REMOTE_SERVER?nodeRef=$nodeRef
			curl --silent -H "Content-Type: application/xml"  -X PUT --data @entity.xml  $LOCAL_SERVER?callback=$REMOTE_SERVER&callbackUser=$USER&callbackPassword=$PASSWORD > nodeRef
			
			export entityNodeRef=`cat nodeRef`
			
			# DATA
			wget --quiet  --http-user=$USER --http-password=$PASSWORD  --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us -O data.xml $REMOTE_SERVER/data?nodeRef=$nodeRef
			curl --silent -H "Content-Type: application/xml"  -X POST --data @data.xml  $LOCAL_SERVER/data?nodeRef=$entityNodeRef
			 
		   count=$((count+1))
		done

      exit 0
fi


wget --http-user=$USER --http-password=$PASSWORD  --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us -O entity.xml $REMOTE_SERVER?nodeRef=$1 
curl -H "Content-Type: application/xml"  -X PUT --data @entity.xml  $LOCAL_SERVER?callback=$REMOTE_SERVER&callbackUser=$USER&callbackPassword=$PASSWORD > nodeRef


# DATA
wget  --http-user=$USER --http-password=$PASSWORD  --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us -O data.xml $REMOTE_SERVER/data?nodeRef=$1 
curl -H "Content-Type: application/xml"  -X POST --data @data.xml  $LOCAL_SERVER/data?nodeRef=`cat nodeRef` > nodeRef
