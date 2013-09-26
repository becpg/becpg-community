#!/bin/sh

. ./common.sh

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
echo "Deploy OLAP Cube"
echo "**********************************************************"
cd $BECPG_OLAP_ROOT/target
tar xvfz becpg-olap-*-distribution.tar.gz
cd becpg-olap-*
./deploy.sh $SERVER
cd $BECPG_OLAP_ROOT/target
rm -rf becpg-olap-*

