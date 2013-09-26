
#!/bin/sh

if [ $# -ne 3 ]
   then
      echo "Usage: $0 <port> <password> <action>"
      exit 0
fi

wget --read-timeout=7200 --delete-after --http-user=admin --http-password=$2 http://localhost:$1/alfresco/service/becpg/migrate/$3

