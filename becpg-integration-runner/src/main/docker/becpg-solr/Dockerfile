FROM alfresco/alfresco-search-services:2.0.13

ENV DIST_DIR=/opt/alfresco-search-services

COPY rerank/solrcore.properties  $DIST_DIR/solrhome/templates/rerank/conf/
COPY rerank/solrconfig.xml  $DIST_DIR/solrhome/templates/rerank/conf/
COPY shared.properties $DIST_DIR/solrhome/conf/
COPY log4j.properties $DIST_DIR/logs/