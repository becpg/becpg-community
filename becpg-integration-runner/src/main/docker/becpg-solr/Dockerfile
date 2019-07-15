FROM openjdk:11
MAINTAINER beCPG

ENV NEXUS=https://artifacts.alfresco.com/nexus/content/groups/public
ENV DIST_DIR /opt/alfresco-search-services
ENV SEARCH_SERVICE_VERSION=1.3.0.6
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
      ${NEXUS}/org/alfresco/alfresco-search-services/${SEARCH_SERVICE_VERSION}/alfresco-search-services-${SEARCH_SERVICE_VERSION}.zip \
      -o alfresco-search-services-${SEARCH_SERVICE_VERSION}.zip  \
    && unzip -q alfresco-search-services-${SEARCH_SERVICE_VERSION}.zip -d /opt/ \
    && mkdir -p $DIST_DIR/data \
    && rm alfresco-search-services-${SEARCH_SERVICE_VERSION}.zip  \
    && chown -R ${USERNAME}:${USERNAME} $DIST_DIR \
    && echo 'SOLR_OPTS="$SOLR_OPTS -Dsolr.data.dir.root=$DIST_DIR/data -Dsolr.solr.model.dir=$DIST_DIR/data/alfrescoModels"' >> $DIST_DIR/solr.in.sh

    
COPY rerank/solrcore.properties  $DIST_DIR/solrhome/templates/rerank/conf/
    
WORKDIR $DIST_DIR
VOLUME $DIST_DIR/data
EXPOSE 8983

USER ${USERNAME}
CMD export SOLR_CREATE_ALFRESCO_DEFAULTS=alfresco,archive && $DIST_DIR/solr/bin/solr start -f
