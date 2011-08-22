#!/bin/sh

. ./common.sh

#becpg-amp
rm $ALF/sdk/lib/server/$BECPG_JAR
rm $ALF/sdk/lib/server/$BECPG_CONFIG_JAR
cd becpg-core
mvn clean package -Dmaven.test.skip=true
cd $TARGET_FOLDER
cp lib/$BECPG_JAR $ALF/sdk/lib/server/
cd config
jar cf becpg-config.jar alfresco/*
cp $BECPG_CONFIG_JAR $ALF/sdk/lib/server/


