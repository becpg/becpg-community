#!/bin/sh

. ./common.sh

#becpg-amp
rm $ALF_SDK/lib/server/$BECPG_JAR
rm $ALF_SDK/lib/server/$BECPG_CONFIG_JAR
cd becpg-core
mvn clean package -Dmaven.test.skip=true
cd $TARGET_FOLDER
cp lib/$BECPG_JAR $ALF_SDK//lib/server/
cd config
jar cf becpg-config.jar alfresco/*
cp $BECPG_CONFIG_JAR $ALF_SDK/lib/server/


