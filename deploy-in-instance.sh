#!/bin/sh

. ./common.sh

if [ $# -ne 1 ]
   then
      echo "Usage: $0 <instance name>"
      exit 0
fi

echo "**********************************************************"
echo "Build AMP"
echo "**********************************************************"

mvn  clean package  -DskipTests=true $MVN_PROFILE


echo "**********************************************************"
echo "Deploy becpg"
echo "**********************************************************"

cd $BECPG_ROOT/distribution/target
buildnumber=`ls becpg-*-distribution.tar.gz`

cd $TC_DIR/deploy
./deploy.sh $1 ${buildnumber%%-distribution.*} distribution
