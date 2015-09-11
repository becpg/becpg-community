#!/bin/sh

export INSTANCE_DIR=/opt/becpg/becpg-tc-server/instances

if [ $# -ne 1 ]
   then
      echo "Usage: $0 <instance name>"
      exit 0
fi


export SERVER=$INSTANCE_DIR/$1

cp -rf src/main/config/* $SERVER/webapps


