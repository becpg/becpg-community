#!/bin/sh
set -e

if [[ $REPO_HOST == "" ]]; then
   REPO_HOST=localhost
fi

if [[ $REPO_PORT == "" ]]; then
   REPO_PORT=8080
fi

if [[ $AI_HOST == "" ]]; then
   AI_HOST=becpg-ai
fi

if [[ $AI_PORT == "" ]]; then
   AI_PORT=8087
fi

if [[ $BECPG_INSTANCE == "" ]]; then
   BECPG_INSTANCE=default
fi

if [[ $OAUTH2_SERVER_URL == "" ]]; then
   OAUTH2_SERVER_URL=http://becpg-auth:8080/auth
fi

if [[ $OAUTH2_RESOURCE == "" ]]; then
   OAUTH2_RESOURCE=inst1-openid
fi

if [[ $OAUTH2_REALM == "" ]]; then
   OAUTH2_REALM=inst1
fi

OAUTH2_ENABLED="false"

BECPG_CONNECTOR_ID="alfresco"
BECPG_EXTERNAL_AUTH="false"

if [[ $BECPG_AUTH_EXTERNAL == "true" ]]; then
   BECPG_CONNECTOR_ID="alfrescoHeader"
   BECPG_EXTERNAL_AUTH="true"
fi

if [[ $BECPG_AUTH_OAUTH2 == "true" ]]; then
    OAUTH2_ENABLED="true"
fi



echo "Replace 'REPO_HOST' with '$REPO_HOST' and 'REPO_PORT' with '$REPO_PORT'"

sed -i -e 's/REPO_HOST:REPO_PORT/'"$REPO_HOST:$REPO_PORT"'/g' /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml
sed -i -e 's/AI_HOST:AI_PORT/'"$AI_HOST:$AI_PORT"'/g' /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml
sed -i -e 's/BECPG_INSTANCE/'"$BECPG_INSTANCE"'/g' /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml
sed -i -e 's/BECPG_CONNECTOR_ID/'"$BECPG_CONNECTOR_ID"'/g' /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml
sed -i -e 's/BECPG_EXTERNAL_AUTH/'"$BECPG_EXTERNAL_AUTH"'/g' /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml
sed -i -e 's/OAUTH2_SECRET/'"$OAUTH2_SECRET"'/g' /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml
sed -i -e 's|OAUTH2_SERVER_URL|'"$OAUTH2_SERVER_URL"'|g' /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml
sed -i -e 's/OAUTH2_RESOURCE/'"$OAUTH2_RESOURCE"'/g' /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml
sed -i -e 's/OAUTH2_REALM/'"$OAUTH2_REALM"'/g' /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml
sed -i -e 's/OAUTH2_ENABLED/'"$OAUTH2_ENABLED"'/g' /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml


echo "NEW -csrf.filter.referer is '$CSRF_FILTER_REFERER'"
echo "NEW -csrf.filter.origin is '$CSRF_FILTER_ORIGIN'"

if [ "${CSRF_FILTER_REFERER}" != "" ] && [  "${CSRF_FILTER_ORIGIN}" != "" ]; then
# set CSRFPolicy to true and set both properties referer and origin
   sed -i -e "s|<config evaluator=\"string-compare\" condition=\"CSRFPolicy\" replace=\"false\">|<config evaluator=\"string-compare\" condition=\"CSRFPolicy\" replace=\"true\">|" /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml
   sed -i -e "s|<referer><\/referer>|<referer>$CSRF_FILTER_REFERER<\/referer>|" /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml
   sed -i -e "s|<origin><\/origin>|<origin>$CSRF_FILTER_ORIGIN<\/origin>|" /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml
   
else
# set CSRFPolicy to false and leave empty the properties referer and origin
   sed -i -e "s|<config evaluator=\"string-compare\" condition=\"CSRFPolicy\" replace=\"false\">|<config evaluator=\"string-compare\" condition=\"CSRFPolicy\" replace=\"false\">|" /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml
   sed -i -e "s|<referer><\/referer>|<referer><\/referer>|" /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml
   sed -i -e "s|<origin><\/origin>|<origin><\/origin>|" /usr/local/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml
fi


bash -c "$@"
