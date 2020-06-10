FROM openjdk:11


ENV DIST_DIR /opt/alfresco-search-services
ENV SEARCH_SERVICE_VERSION=1.4.2
ENV ZIP_FILE=https://download.alfresco.com/cloudfront/release/community/SearchServices/${SEARCH_SERVICE_VERSION}/alfresco-search-services-${SEARCH_SERVICE_VERSION}.zip
ARG USERNAME=solr
ARG USERID=33007

# Default template "rerank"
# SOLR_ALFRESCO_HOST=becpg
# SOLR_ALFRESCO_PORT=8080
# SOLR_SOLR_HOST=solr
# SOLR_SOLR_PORT=8983
# SOLR_CREATE_ALFRESCO_DEFAULTS=alfresco,archive

RUN set -x \
	&& useradd  -c "beCPG ${USERNAME}" -M  -s "/bin/bash"  -u "${USERID}" -o   "${USERNAME}" \
    && curl --silent --location \
      $ZIP_FILE \
      -o alfresco-search-services-${SEARCH_SERVICE_VERSION}.zip  \
    && unzip -q alfresco-search-services-${SEARCH_SERVICE_VERSION}.zip -d /opt/ \
    && mkdir -p $DIST_DIR/data/solr6Backup/ \
    && rm alfresco-search-services-${SEARCH_SERVICE_VERSION}.zip  \
    && chown -R ${USERNAME}:${USERNAME} $DIST_DIR 

    
COPY solr.in.sh  $DIST_DIR/solr.in.sh    
COPY rerank/solrcore.properties  $DIST_DIR/solrhome/templates/rerank/conf/
COPY shared.properties $DIST_DIR/solrhome/conf/
    
WORKDIR $DIST_DIR
VOLUME $DIST_DIR/data
EXPOSE 8983

USER ${USERNAME}
CMD export SOLR_CREATE_ALFRESCO_DEFAULTS=alfresco,archive && $DIST_DIR/solr/bin/solr start -f
