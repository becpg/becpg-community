#!/bin/sh

. ./common.sh



echo "**********************************************************"
echo "Build AMP"
echo "**********************************************************"

mvn clean package -Dmaven.test.skip=true $MVN_PROFILE


echo "**********************************************************"
echo "Send to releases server"
echo "**********************************************************"

cd $BECPG_ROOT/distribution/target
scp -P 22222 becpg-*-distribution.tar.gz  root@www.becpg.fr:/srv/becpg-data/becpg-repo/releases

cd $BECPG_OLAP_ROOT/target
scp -P 22222 becpg-olap-*-distribution.tar.gz  root@www.becpg.fr:/srv/becpg-data/becpg-repo/releases

