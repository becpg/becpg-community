FROM ${docker.share.image}:${alfresco.share.docker.version}

USER root

ENV TOMCAT_DIR=/usr/local/tomcat

ARG GROUPNAME=Alfresco
ARG GROUPID=1000
ARG IMAGEUSERNAME=alfresco
ARG USERID=33000

ENV NEXUS=https://artifacts.alfresco.com/nexus/content/groups/public
ENV TOMCAT_DIR=/usr/local/tomcat

COPY tomcat/server.xml $TOMCAT_DIR/conf/server.xml
COPY tomcat/share.xml $TOMCAT_DIR/conf/Catalina/localhost/
COPY becpg/classes $TOMCAT_DIR/shared/classes

RUN dnf update -y && dnf clean all && \
    chmod +x /usr/local/tomcat/shared/classes/alfresco/substituter.sh

RUN mkdir -p ${TOMCAT_DIR}/shared/classes/alfresco/messages/custom && \
    chown -R ${USERID}:${GROUPID} ${TOMCAT_DIR}/shared/classes/alfresco/messages && \
    chmod -R g+w ${TOMCAT_DIR}/shared/classes/alfresco/messages

RUN mkdir -p ${TOMCAT_DIR}/data
      

RUN mkdir -p ${TOMCAT_DIR}/conf/Catalina/localhost && \
         groupadd -g ${GROUPID} ${GROUPNAME} && \
         useradd -u ${USERID} -G ${GROUPNAME} ${IMAGEUSERNAME} && \
         chgrp -R ${GROUPNAME} ${TOMCAT_DIR} && \
         chmod g+rx ${TOMCAT_DIR}/conf && \
         chmod -R g+r ${TOMCAT_DIR}/conf && \
         chmod -R g+w ${TOMCAT_DIR}/shared/classes/alfresco/web-extension && \
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