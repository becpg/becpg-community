FROM openjdk:11.0.8-jdk

ENV DIST_DIR /opt/alfresco-search-services
ENV SEARCH_SERVICE_VERSION=1.4.3
ENV ZIP_FILE=https://download.alfresco.com/cloudfront/release/community/SearchServices/${SEARCH_SERVICE_VERSION}/alfresco-search-services-${SEARCH_SERVICE_VERSION}.zip
ENV LANG C.UTF-8

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
    && mkdir -p $DIST_DIR/data/solr6Backup/alfresco \
    && mkdir -p $DIST_DIR/data/solr6Backup/archive \
    && mv $DIST_DIR/solrhome/alfrescoModels $DIST_DIR/data/ \
    && rm alfresco-search-services-${SEARCH_SERVICE_VERSION}.zip  \
    && chown -R ${USERNAME}:${USERNAME} $DIST_DIR 

# Cloud profiling

RUN mkdir -p /opt/cprof && \
  wget -q -O- https://storage.googleapis.com/cloud-profiler/java/latest/profiler_java_agent.tar.gz \
  | tar xzv -C /opt/cprof

    
COPY solr.in.sh  $DIST_DIR/solr.in.sh    
RUN chmod +x $DIST_DIR/solr.in.sh
COPY rerank/solrcore.properties  $DIST_DIR/solrhome/templates/rerank/conf/
COPY rerank/solrconfig.xml  $DIST_DIR/solrhome/templates/rerank/conf/
COPY shared.properties $DIST_DIR/solrhome/conf/
COPY search_config_setup.sh $DIST_DIR/solr/bin/
RUN  cp -rf $DIST_DIR/solrhome $DIST_DIR/solrhome_init
RUN chmod +x $DIST_DIR/solr/bin/search_config_setup.sh
#COPY log4j.properties $DIST_DIR/logs/
    
WORKDIR $DIST_DIR
VOLUME $DIST_DIR/data
EXPOSE 8983

USER ${USERNAME}
CMD  $DIST_DIR/solr/bin/search_config_setup.sh "$DIST_DIR/solr/bin/solr start -f"
