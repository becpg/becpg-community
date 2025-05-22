FROM ${docker.share.image}:${alfresco.share.docker.version}

USER root

ENV TOMCAT_DIR=/usr/local/tomcat

ARG GROUPNAME=Alfresco
ARG GROUPID=1000
ARG IMAGEUSERNAME=alfresco
ARG USERID=33000

ENV NEXUS=https://artifacts.alfresco.com/nexus/content/groups/public
ENV SUPPORT_TOOLS_VERSION=${becpg.supporttools.version}
ENV TOMCAT_DIR=/usr/local/tomcat
ENV I18_VERSION=${becpg.i18n.version}

COPY tomcat/server.xml $TOMCAT_DIR/conf/server.xml
COPY tomcat/share.xml $TOMCAT_DIR/conf/Catalina/localhost/
COPY becpg/classes $TOMCAT_DIR/shared/classes

RUN chmod +x /usr/local/tomcat/shared/classes/alfresco/substituter.sh
RUN mkdir -p ${TOMCAT_DIR}/data


#Support tools
RUN curl --silent --location \
      https://storage.googleapis.com/becpg-repo/addons/support-tools-share-${SUPPORT_TOOLS_VERSION}.amp  \
      -o $TOMCAT_DIR/amps_share/support-tools-share-${SUPPORT_TOOLS_VERSION}.amp && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps_share/ webapps/share -nobackup -directory && \
    rm $TOMCAT_DIR/amps_share/support-tools-share-${SUPPORT_TOOLS_VERSION}.amp && rm -rf  $TOMCAT_DIR/amps_share/* 

# I18N
RUN curl --silent --location \
     https://storage.googleapis.com/becpg-repo/addons/share-becpg-i18n-${I18_VERSION}.jar  \
     -o $TOMCAT_DIR/webapps/share/WEB-INF/lib/share-becpg-i18n-${I18_VERSION}.jar 
      

RUN mkdir -p ${TOMCAT_DIR}/conf/Catalina/localhost && \
         groupadd -g ${GROUPID} ${GROUPNAME} && \
         useradd -u ${USERID} -G ${GROUPNAME} ${IMAGEUSERNAME} && \
         chgrp -R ${GROUPNAME} ${TOMCAT_DIR} && \
         chmod g+rx ${TOMCAT_DIR}/conf && \
         chmod -R g+r ${TOMCAT_DIR}/conf && \
         find ${TOMCAT_DIR}/webapps -type d -exec chmod 0750 {} \; && \
         find ${TOMCAT_DIR}/webapps -type f -exec chmod 0640 {} \; && \
         chmod -R g+r ${TOMCAT_DIR}/webapps && \
         chmod g+r ${TOMCAT_DIR}/conf/Catalina && \
         chmod g+rwx ${TOMCAT_DIR}/data && \
         chmod g+rwx ${TOMCAT_DIR}/logs && \
         chmod o-w ${TOMCAT_DIR}/logs && \
         chmod g+rwx ${TOMCAT_DIR}/temp && \
         chmod g+rwx ${TOMCAT_DIR}/work && \
         chmod o-w ${TOMCAT_DIR}/work && \
         chmod 664 ${TOMCAT_DIR}/alfresco-mmt/alfresco-mmt-*.jar && \
         find /licenses -type d -exec chmod 0755 {} \; && \
         find /licenses -type f -exec chmod 0644 {} \;