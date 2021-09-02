FROM ${docker.acs.image}:${alfresco.platform.version}

ARG TOMCAT_DIR=/usr/local/tomcat

USER root

RUN yum -y install glibc-locale-source glibc-langpack-en


#ENV VTI_VERSION=${alfresco.aos.version}
ENV MYSQL_CONNECTOR=${mysql.connector.version}
ENV SUPPORT_TOOLS_VERSION=1.2.0.0-beCPG-amp
      
    

## JDBC - MYSQL
RUN curl --silent --location \
      https://repo1.maven.org/maven2/mysql/mysql-connector-java/${MYSQL_CONNECTOR}/mysql-connector-java-${MYSQL_CONNECTOR}.jar \
	-o $TOMCAT_DIR/lib/mysql-connector-java-${MYSQL_CONNECTOR}.jar && \
	rm $TOMCAT_DIR/lib/postgres*



#Javascript console
RUN curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/javascript-console-repo-0.6.0.amp  \
      -o $TOMCAT_DIR/amps/javascript-console-repo-0.6.0.amp && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps/ $TOMCAT_DIR/webapps/alfresco -nobackup -directory && \
    rm $TOMCAT_DIR/amps/javascript-console-repo-0.6.0.amp  
    
    
#Support tools
RUN curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/support-tools-repo-${SUPPORT_TOOLS_VERSION}.amp  \
      -o $TOMCAT_DIR/amps/support-tools-repo-${SUPPORT_TOOLS_VERSION}.amp && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps/ $TOMCAT_DIR/webapps/alfresco -nobackup -directory && \
    rm $TOMCAT_DIR/amps/support-tools-repo-${SUPPORT_TOOLS_VERSION}.amp  



#AOS    


#TODO _Vti_bin.war
        	        
#RUN curl --silent --location \
#     ${NEXUS}/org/alfresco/aos-module/alfresco-aos-module/${VTI_VERSION}/alfresco-aos-module-${VTI_VERSION}.amp  \
#      -o $TOMCAT_DIR/amps/alfresco-aos-core-${VTI_VERSION}.amp && \
#    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps/ $TOMCAT_DIR/webapps/alfresco -nobackup -directory && \
#    rm $TOMCAT_DIR/amps/alfresco-aos-core-${VTI_VERSION}.amp



# Move to enterprise Cloud profiling

#RUN mkdir -p /opt/cprof && \
#   curl --silent --location https://storage.googleapis.com/cloud-profiler/java/latest/profiler_java_agent.tar.gz \
#   -o profiler_java_agent.tar.gz \
#  | tar xzv -C /opt/cprof

# I18N


RUN curl --silent --location \
      https://github.com/becpg/becpg-i18n/releases/download/1.0-74/alfresco-becpg-i18n-1.0-74.jar  \
      -o webapps/alfresco/WEB-INF/lib/alfresco-becpg-i18n-1.0-74.jar

      
COPY tomcat/server.xml $TOMCAT_DIR/conf/server.xml


COPY becpg/classes /usr/local/tomcat/shared/classes


ENV JAVA_OPTS "-Ddir.root=/usr/local/tomcat/data"

USER ${USERNAME}
