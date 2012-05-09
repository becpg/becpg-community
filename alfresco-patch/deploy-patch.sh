#!/bin/sh

. ../common.sh

if [ $# -ne 1 ]
   then
      echo "Usage: $0 <instance name>"
      exit 0
fi


export SERVER=$INSTANCE_DIR/$1

echo "**********************************************************"
echo "Build AMP"
echo "**********************************************************"

mvn clean package -Dmaven.test.skip=true $MVN_PROFILE


echo "**********************************************************"
echo "Deploy becpg"
echo "**********************************************************"

cd $BECPG_ROOT/distribution/target
tar xvfz alfresco-patch-*-distribution.tar.gz
cd alfresco-patch-*
./deploy.sh $SERVER


