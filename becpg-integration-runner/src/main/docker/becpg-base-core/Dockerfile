FROM ${docker.acs.image}:${alfresco.platform.version}

ENV TOMCAT_DIR=/usr/local/tomcat

USER root

ENV NEXUS=https://artifacts.alfresco.com/nexus/content/groups/public
ENV AOS_VERSION=${alfresco.aos.version}
ENV MYSQL_CONNECTOR=${mysql.connector.version}
ENV SUPPORT_TOOLS_VERSION=${becpg.supporttools.version}
ENV I18_VERSION=${becpg.i18n.version}
   
## JDBC - MYSQL
RUN curl --silent --location \
      https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/${MYSQL_CONNECTOR}/mysql-connector-j-${MYSQL_CONNECTOR}.jar \
	-o $TOMCAT_DIR/lib/mysql-connector-j-${MYSQL_CONNECTOR}.jar && \
	rm $TOMCAT_DIR/lib/postgres*

#Support tools
RUN curl --silent --location \
      https://storage.googleapis.com/becpg-repo/addons/support-tools-repo-${SUPPORT_TOOLS_VERSION}.amp  \
      -o $TOMCAT_DIR/amps/support-tools-repo-${SUPPORT_TOOLS_VERSION}.amp && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps/ $TOMCAT_DIR/webapps/alfresco -nobackup -directory && \
    rm $TOMCAT_DIR/amps/support-tools-repo-${SUPPORT_TOOLS_VERSION}.amp  


#AOS  	        
RUN   curl --silent --location \
     ${NEXUS}/org/alfresco/aos-module/alfresco-aos-module/${AOS_VERSION}/alfresco-aos-module-${AOS_VERSION}.amp  \
      -o $TOMCAT_DIR/amps/alfresco-aos-core-${AOS_VERSION}.amp && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps/ $TOMCAT_DIR/webapps/alfresco -force -nobackup -directory && \
    rm $TOMCAT_DIR/amps/alfresco-aos-core-${AOS_VERSION}.amp


# I18N
RUN set -x && \
    curl --silent --location \
      https://storage.googleapis.com/becpg-repo/addons/alfresco-becpg-i18n-${I18_VERSION}.jar  \
      -o $TOMCAT_DIR/webapps/alfresco/WEB-INF/lib/alfresco-becpg-i18n-${I18_VERSION}.jar

      
COPY tomcat/server.xml $TOMCAT_DIR/conf/server.xml
COPY becpg/classes $TOMCAT_DIR/shared/classes
RUN rm -rf  $TOMCAT_DIR/amps/*
RUN mkdir -p ${TOMCAT_DIR}/data && rm -rf  $TOMCAT_DIR/alf_data

ENV JAVA_OPTS "-Ddir.root=/usr/local/tomcat/data -XX:+ExitOnOutOfMemoryError -XX:+UseG1GC -XX:MaxGCPauseMillis=200  -XX:+ParallelRefProcEnabled -Xss512k -XX:+UseStringDeduplication"
