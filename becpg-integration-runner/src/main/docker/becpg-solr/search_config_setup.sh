#!/bin/bash
set -e
# By default its going to deploy "Master" setup configuration with "REPLICATION_TYPE=master".
# Slave replica service can be enabled using "REPLICATION_TYPE=slave" environment value.

RERANK_TEMPLATE_PATH=$PWD/solrhome/templates/rerank/conf
NORERANK_TEMPLATE_PATH=$PWD/solrhome/templates/noRerank/conf
SOLR_RERANK_CONFIG_FILE=$RERANK_TEMPLATE_PATH/solrconfig.xml
SOLR_NORERANK_CONFIG_FILE=$NORERANK_TEMPLATE_PATH/solrconfig.xml
SOLR_RERANK_CORE_FILE=$RERANK_TEMPLATE_PATH/solrcore.properties
SOLR_NORERANK_CORE_FILE=$NORERANK_TEMPLATE_PATH/solrcore.properties
SOLR_CONTEXT_FILE=$PWD/solr/server/contexts/solr-jetty-context.xml

if [[ $REPLICATION_TYPE == "master" ]]; then

   findStringMaster='<requestHandler name="\/replication" class="org\.alfresco\.solr\.handler\.AlfrescoReplicationHandler">'

   replaceStringMaster="\n\t<lst name=\"master\"> \n"

   if [[ $REPLICATION_AFTER == "" ]]; then
      REPLICATION_AFTER=commit,startup
   fi

   if [[ $REPLICATION_CONFIG_FILES == "" ]]; then
      REPLICATION_CONFIG_FILES=schema.xml,stopwords.txt
   fi

   for i in $(echo $REPLICATION_AFTER | sed "s/,/ /g")
   do
      replaceStringMaster+="\t\t<str name=\"replicateAfter\">"$i"<\/str> \n"
   done

   if [[ ! -z "$REPLICATION_CONFIG_FILES" ]]; then
      replaceStringMaster+="\t\t<str name=\"confFiles\">$REPLICATION_CONFIG_FILES<\/str> \n"
   fi

   replaceStringMaster+="\t<\/lst>"

   sed -i "s/$findStringMaster/$findStringMaster$replaceStringMaster/g" $SOLR_RERANK_CONFIG_FILE $SOLR_NORERANK_CONFIG_FILE
   sed -i "s/enable.alfresco.tracking=true/enable.alfresco.tracking=true\nenable.master=true\nenable.slave=false/g" $SOLR_RERANK_CORE_FILE $SOLR_NORERANK_CORE_FILE
fi

if [[ $REPLICATION_TYPE == "slave" ]]; then

   if [[ $REPLICATION_MASTER_PROTOCOL != https ]]; then
      REPLICATION_MASTER_PROTOCOL=http
   fi

   if [[ $REPLICATION_MASTER_HOST == "" ]]; then
      REPLICATION_MASTER_HOST=localhost
   fi

   if [[ $REPLICATION_MASTER_PORT == "" ]]; then
      REPLICATION_MASTER_PORT=8083
   fi

   if [[ $REPLICATION_POLL_INTERVAL == "" ]]; then
      REPLICATION_POLL_INTERVAL=00:00:30
   fi

   sed -i 's/<requestHandler name="\/replication" class="org\.alfresco\.solr\.handler\.AlfrescoReplicationHandler">/<requestHandler name="\/replication" class="org\.alfresco\.solr\.handler\.AlfrescoReplicationHandler">\
      <lst name="slave">\
         <str name="masterUrl">'$REPLICATION_MASTER_PROTOCOL':\/\/'$REPLICATION_MASTER_HOST':'$REPLICATION_MASTER_PORT'\/solr\/${solr.core.name}<\/str>\
         <str name="pollInterval">'$REPLICATION_POLL_INTERVAL'<\/str>\
      <\/lst>/g' $SOLR_RERANK_CONFIG_FILE $SOLR_NORERANK_CONFIG_FILE
   sed -i "s/enable.alfresco.tracking=true/enable.alfresco.tracking=false\nenable.master=false\nenable.slave=true/g" $SOLR_RERANK_CORE_FILE $SOLR_NORERANK_CORE_FILE
   sed -i 's/default="\/solr"/default="\/solr-slave"/g' $SOLR_CONTEXT_FILE
fi

bash -c "$@"