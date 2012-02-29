#!/bin/bash
#mavenRemoteDeploy.sh
#author: Massimiliano Marcon

if [ $# -lt 4 ]; then
    echo "Usage: $0 <group> <artifact> <version> <filename> [remote repository]";
    exit 0;
fi

#replace $DOC_ROOT with the correct path on the server
MAVEN_DEF_REMOTE_REPO="scp://becpg.fr/srv/becpg-data/maven"
REPO_ID="becpg-repo" #if this repo id is specified in the settings.xml file w/ related username and pwd the authentication will be password-less
MAVEN_PLUGIN="deploy:deploy-file"

GROUP=$1
ARTIFACT=$2
VERSION=$3
FILE=$4
MAVEN_REMOTE_REPO=$5
TYPE="jar"

if [ "$MAVEN_REMOTE_REPO" = "" ]; then
    echo "No remote repository specified: using default (${MAVEN_DEF_REMOTE_REPO})"
    MAVEN_REMOTE_REPO=$MAVEN_DEF_REMOTE_REPO
fi

echo "Adding ${FILE} to the repository..."
mvn $MAVEN_PLUGIN -Durl=$MAVEN_REMOTE_REPO -DrepositoryId=$REPO_ID -DgroupId=$GROUP -DartifactId=$ARTIFACT -Dversion=$VERSION -Dpackaging=$TYPE -Dfile=$FILE


