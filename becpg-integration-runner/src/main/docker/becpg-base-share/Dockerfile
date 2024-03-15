FROM ${docker.share.image}:${alfresco.share.docker.version}

ENV TOMCAT_DIR=/usr/local/tomcat

ENV I18_VERSION=1.0-122
ENV SUPPORT_TOOLS_VERSION=1.2.0.0-beCPG-amp

USER root

#RUN yum update -y && yum -y install glibc-locale-source glibc-langpack-en  && yum clean all && \
#    rm -rf /var/cache/yum

ENV NEXUS=https://artifacts.alfresco.com/nexus/content/groups/public
ENV SUPPORT_TOOLS_VERSION=1.2.0.0-beCPG-amp

#Javascript console
RUN curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/javascript-console-share-0.7.0.amp  \
      -o $TOMCAT_DIR/amps_share/javascript-console-share-0.7.0.amp && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps_share/ webapps/share -nobackup -directory && \
    rm $TOMCAT_DIR/amps_share/javascript-console-share-0.7.0.amp 
    
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

RUN rm -rf  $TOMCAT_DIR/amps_share/*

USER ${USERNAME}
