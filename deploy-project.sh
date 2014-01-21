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

mvn -T 2C clean package  -Dmaven.test.skip=true $MVN_PROFILE


echo "**********************************************************"
echo "Deploy becpg"
echo "**********************************************************"

cd $BECPG_ROOT/distribution/target
buildnumber=`ls becpg-*-becpg-project.tar.gz`

cd $TC_DIR/deploy
echo "./deploy.sh $1 ${buildnumber%%-becpg-project.*} becpg-project"
./deploy.sh $1 ${buildnumber%%-becpg-project.*} becpg-project
