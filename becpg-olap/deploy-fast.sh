#!/bin/sh

. ../common.sh

if [ $# -ne 1 ]
   then
      echo "Usage: $0 <instance name>"
      exit 0
fi


export SERVER=$INSTANCE_DIR/$1

cp -rf _diff/* $SERVER/webapps


