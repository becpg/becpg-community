#!/bin/sh

. ./common.sh

mvn clean package -Dmaven.test.skip=true

#clean
rm $ALF_SDK/lib/server/becpg-commons-$BECPG_VERSION.jar
rm $ALF_SDK/lib/server/becpg-core-$BECPG_VERSION.jar
rm $ALF_SDK/lib/server/becpg-config.jar

#becpg-commons
cp becpg-commons/target/becpg-commons-$BECPG_VERSION.jar $ALF_SDK/lib/server/

#becpg-core
cp becpg-core/target/becpg-core-$BECPG_VERSION/lib/becpg-core-$BECPG_VERSION.jar $ALF_SDK/lib/server/

#becpg config
cd becpg-core/target/becpg-core-$BECPG_VERSION/config
jar cf becpg-config.jar alfresco/*
cp becpg-config.jar $ALF_SDK/lib/server/


