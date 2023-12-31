FROM ${docker.acs.image}:${alfresco.platform.version}

ENV TOMCAT_DIR=/usr/local/tomcat

USER root

#RUN yum update -y && yum -y install glibc-locale-source glibc-langpack-en  && yum clean all && \
#    rm -rf /var/cache/yum

ENV NEXUS=https://artifacts.alfresco.com/nexus/content/groups/public
ENV AOS_VERSION=${alfresco.aos.version}
ENV MYSQL_CONNECTOR=${mysql.connector.version}
ENV SUPPORT_TOOLS_VERSION=1.3.0.0-beCPG-amp
ENV I18_VERSION=1.0-111
   
## JDBC - MYSQL
RUN curl --silent --location \
      https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/${MYSQL_CONNECTOR}/mysql-connector-j-${MYSQL_CONNECTOR}.jar \
	-o $TOMCAT_DIR/lib/mysql-connector-j-${MYSQL_CONNECTOR}.jar && \
	rm $TOMCAT_DIR/lib/postgres*


#Javascript console
RUN curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/javascript-console-repo-0.7.0.amp  \
      -o $TOMCAT_DIR/amps/javascript-console-repo-0.7.0.amp && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps/ $TOMCAT_DIR/webapps/alfresco -nobackup -directory && \
    rm $TOMCAT_DIR/amps/javascript-console-repo-0.7.0.amp  
    
    
#Support tools
RUN curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/support-tools-repo-${SUPPORT_TOOLS_VERSION}.amp  \
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
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/alfresco-becpg-i18n-${I18_VERSION}.jar  \
      -o $TOMCAT_DIR/webapps/alfresco/WEB-INF/lib/alfresco-becpg-i18n-${I18_VERSION}.jar

      
COPY tomcat/server.xml $TOMCAT_DIR/conf/server.xml
COPY becpg/classes $TOMCAT_DIR/shared/classes
RUN rm -rf  $TOMCAT_DIR/amps/*

ENV JAVA_OPTS "-Ddir.root=/usr/local/tomcat/data"

USER ${USERNAME}
