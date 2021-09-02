FROM ${docker.share.image}:${alfresco.share.version}

ENV SUPPORT_TOOLS_VERSION=1.2.0.0-beCPG-amp

ARG TOMCAT_DIR=/usr/local/tomcat

RUN yum -y install glibc-locale-source glibc-langpack-en


## Addons
RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/workflow-reporting-share-1.0.1.amp  \
      -o $TOMCAT_DIR/amps_share/workflow-reporting-share-1.0.1.amp && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps_share/ webapps/share -nobackup -directory && \
    rm $TOMCAT_DIR/amps_share/workflow-reporting-share-1.0.1.amp     
    

RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/javascript-console-share-0.6.0.amp  \
      -o $TOMCAT_DIR/amps_share/javascript-console-share-0.6.0.amp && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps_share/ webapps/share -nobackup -directory && \
    rm $TOMCAT_DIR/amps_share/javascript-console-share-0.6.0.amp 
    

#RUN set -x && \
#    curl --silent --location \
#      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/support-tools-share-${SUPPORT_TOOLS_VERSION}.amp  \
#      -o $TOMCAT_DIR/amps_share/support-tools-share-${SUPPORT_TOOLS_VERSION}.amp && \
#    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps_share/ webapps/share -nobackup -directory && \
#    rm $TOMCAT_DIR/amps_share/support-tools-share-${SUPPORT_TOOLS_VERSION}.amp  


# Cloud profiling Move to enterprise

#RUN mkdir -p /opt/cprof && \
#  wget -q -O- https://storage.googleapis.com/cloud-profiler/java/latest/profiler_java_agent.tar.gz \
#  | tar xzv -C /opt/cprof

# I18N

RUN set -x && \
   curl --silent --location \
      https://github.com/becpg/becpg-i18n/releases/download/1.0-74/share-becpg-i18n-1.0-74.jar  \
     -o webapps/share/WEB-INF/lib/share-becpg-i18n-1.0-74.jar 

      
#Tuning (TODO a verifier)
      
COPY tomcat/server.xml $TOMCAT_DIR/conf/server.xml
COPY tomcat/share.xml $TOMCAT_DIR/conf/Catalina/localhost/
#COPY tomcat/catalina.properties /usr/local/tomcat/conf/catalina.properties
#COPY tomcat/logging.properties /usr/local/tomcat/conf/logging.properties

COPY becpg/classes /usr/local/tomcat/shared/classes



USER ${USERNAME}

