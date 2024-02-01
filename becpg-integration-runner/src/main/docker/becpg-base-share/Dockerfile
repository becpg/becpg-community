FROM ${docker.share.image}:${alfresco.share.docker.version}


USER root

ENV NEXUS=https://artifacts.alfresco.com/nexus/content/groups/public
ENV SUPPORT_TOOLS_VERSION=${becpg.supporttools.version}
ENV TOMCAT_DIR=/usr/local/tomcat
ENV I18_VERSION=${becpg.i18n.version}

#Support tools
RUN curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/support-tools-share-${SUPPORT_TOOLS_VERSION}.amp  \
      -o $TOMCAT_DIR/amps_share/support-tools-share-${SUPPORT_TOOLS_VERSION}.amp && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps_share/ webapps/share -nobackup -directory && \
    rm $TOMCAT_DIR/amps_share/support-tools-share-${SUPPORT_TOOLS_VERSION}.amp  

# I18N
RUN curl --silent --location \
     https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/share-becpg-i18n-${I18_VERSION}.jar  \
     -o $TOMCAT_DIR/webapps/share/WEB-INF/lib/share-becpg-i18n-${I18_VERSION}.jar 
      
COPY tomcat/server.xml $TOMCAT_DIR/conf/server.xml
COPY tomcat/share.xml $TOMCAT_DIR/conf/Catalina/localhost/
COPY becpg/classes $TOMCAT_DIR/shared/classes

RUN chmod +x /usr/local/tomcat/shared/classes/alfresco/substituter.sh
RUN mkdir -p ${TOMCAT_DIR}/data

RUN rm -rf  $TOMCAT_DIR/amps_share/*
