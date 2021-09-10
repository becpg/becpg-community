FROM tomcat:9-jdk11

ENV NEXUS=https://artifacts.alfresco.com/nexus/content/groups/public
ENV ALF_VERSION=${alfresco.platform.version}
ENV VTI_VERSION=${alfresco.aos.version}
ENV ALF_SHARE_VERSION=${alfresco.share.version}
ENV MYSQL_CONNECTOR=${mysql.connector.version}
ENV SUPPORT_TOOLS_VERSION=1.2.0.0-beCPG-amp

WORKDIR /usr/local/tomcat/

ARG VERSION

RUN  rm -rf /usr/share/doc \
                webapps/docs \
                webapps/examples \
                webapps/manager \
                webapps/host-manager \
                webapps/ROOT


RUN mkdir -p /usr/local/tomcat/data && \
	mkdir -p /usr/local/tomcat/lib/classes && \
	mkdir -p /opt/tools && \
	mkdir -p /usr/local/tomcat/shared/lib && \
    mkdir -p /usr/local/tomcat/shared/classes/alfresco/extension/mimetypes \
             /usr/local/tomcat/shared/classes/alfresco/extension/transform/renditions \
             /usr/local/tomcat/shared/classes/alfresco/extension/transform/pipelines 
	


RUN set -x \
     && apt-get update \
      && apt-get install -y --no-install-recommends \
                  imagemagick \
                 ghostscript \
      && apt-get clean \
      && rm -rf /var/lib/apt/lists/*
      
      
      
WORKDIR /usr/local/tomcat/
      

      
RUN set -x && \
    curl --silent --location \  
       ${NEXUS}/org/alfresco/alfresco-content-services-community-distribution/${ALF_VERSION}/alfresco-content-services-community-distribution-${ALF_VERSION}.zip \
       -o /root/alfresco-content-services-community-distribution.zip && \
       cd /root && unzip /root/alfresco-content-services-community-distribution.zip && cd /usr/local/tomcat/ && \
       tar xvfz /root/alfresco-content-services-community-distribution-${ALF_VERSION}/alfresco-pdf-renderer/alfresco-pdf-renderer-1.1-linux.tgz -C /opt/tools/ && \
       unzip -q /root/alfresco-content-services-community-distribution-${ALF_VERSION}/web-server/webapps/alfresco.war -d webapps/alfresco && \
       unzip -q /root/alfresco-content-services-community-distribution-${ALF_VERSION}/web-server/webapps/share.war -d webapps/share && \
       unzip -q /root/alfresco-content-services-community-distribution-${ALF_VERSION}/web-server/webapps/_vti_bin.war -d webapps/_vti_bin && \
       unzip -q /root/alfresco-content-services-community-distribution-${ALF_VERSION}/web-server/webapps/ROOT.war -d webapps/ROOT && \
       mkdir /root/amp && cp /root/alfresco-content-services-community-distribution-${ALF_VERSION}/bin/alfresco-mmt.jar /root/ &&  \
       java -jar /root/alfresco-mmt.jar install /root/alfresco-content-services-community-distribution-${ALF_VERSION}/amps/ webapps/alfresco -nobackup -directory && \
       rm -rf /root/alfresco-content-services-community-distribution-${ALF_VERSION} && rm -rf /root/alfresco-content-services-community-distribution.zip
      
    

## JDBC - MYSQL
RUN set -x && \
    curl --silent --location \
      https://repo1.maven.org/maven2/mysql/mysql-connector-java/${MYSQL_CONNECTOR}/mysql-connector-java-${MYSQL_CONNECTOR}.jar \
	-o lib/mysql-connector-java-${MYSQL_CONNECTOR}.jar



#workflow-reporting-repo 
RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/workflow-reporting-repo-1.0.1.amp  \
      -o /root/amp/workflow-reporting-repo-1.0.1.amp && \
    java -jar /root/alfresco-mmt.jar install /root/amp/ webapps/alfresco -nobackup -directory && \
    rm /root/amp/workflow-reporting-repo-1.0.1.amp      

RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/workflow-reporting-share-1.0.1.amp  \
      -o /root/amp/workflow-reporting-share-1.0.1.amp && \
    java -jar /root/alfresco-mmt.jar install /root/amp/ webapps/share -nobackup -directory && \
    rm /root/amp/workflow-reporting-share-1.0.1.amp     
    

#Javascript console
RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/javascript-console-repo-0.6.0.amp  \
      -o /root/amp/javascript-console-repo-0.6.0.amp && \
    java -jar /root/alfresco-mmt.jar install /root/amp/ webapps/alfresco -nobackup -directory && \
    rm /root/amp/javascript-console-repo-0.6.0.amp  
    
RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/javascript-console-share-0.6.0.amp  \
      -o /root/amp/javascript-console-share-0.6.0.amp && \
    java -jar /root/alfresco-mmt.jar install /root/amp/ webapps/share -nobackup -directory && \
    rm /root/amp/javascript-console-share-0.6.0.amp 
    
#User account switcher  
#RUN set -x && \
#    curl --silent --location \
#      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/share-login-switcher-1.5.jar  \
#      -o webapps/share/WEB-INF/lib/share-login-switcher-1.5.jar    
      
#RUN set -x && \
#    curl --silent --location \
#      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/repo-login-switcher-1.5.jar  \
#      -o webapps/alfresco/WEB-INF/lib/repo-login-switcher-1.5.jar          
    
#Support tools
RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/support-tools-repo-${SUPPORT_TOOLS_VERSION}.amp  \
      -o /root/amp/support-tools-repo-${SUPPORT_TOOLS_VERSION}.amp && \
    java -jar /root/alfresco-mmt.jar install /root/amp/ webapps/alfresco -nobackup -directory && \
    rm /root/amp/support-tools-repo-${SUPPORT_TOOLS_VERSION}.amp  


RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/support-tools-share-${SUPPORT_TOOLS_VERSION}.amp  \
      -o /root/amp/support-tools-share-${SUPPORT_TOOLS_VERSION}.amp && \
    java -jar /root/alfresco-mmt.jar install /root/amp/ webapps/share -nobackup -directory && \
    rm /root/amp/support-tools-share-${SUPPORT_TOOLS_VERSION}.amp  

#AOS    
        	        
RUN set -x && \
    curl --silent --location \
     ${NEXUS}/org/alfresco/aos-module/alfresco-aos-module/${VTI_VERSION}/alfresco-aos-module-${VTI_VERSION}.amp  \
      -o /root/amp/alfresco-aos-core-${VTI_VERSION}.amp && \
    java -jar /root/alfresco-mmt.jar install /root/amp/ webapps/alfresco -nobackup -directory && \
    rm /root/amp/alfresco-aos-core-${VTI_VERSION}.amp



# Cloud profiling

RUN mkdir -p /opt/cprof && \
  wget -q -O- https://storage.googleapis.com/cloud-profiler/java/latest/profiler_java_agent.tar.gz \
  | tar xzv -C /opt/cprof

# I18N

RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/share-becpg-i18n-1.0-64.jar  \
      -o webapps/share/WEB-INF/lib/share-becpg-i18n-1.0-64.jar  

RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/alfresco-becpg-i18n-1.0-64.jar  \
      -o webapps/alfresco/WEB-INF/lib/alfresco-becpg-i18n-1.0-64.jar  
      
      
COPY tomcat/share.xml /usr/local/tomcat/conf/Catalina/localhost/
COPY tomcat/catalina.properties /usr/local/tomcat/conf/catalina.properties
COPY tomcat/web.xml /usr/local/tomcat/conf/web.xml
COPY tomcat/server.xml /usr/local/tomcat/conf/server.xml
COPY tomcat/logging.properties /usr/local/tomcat/conf/logging.properties

COPY becpg/classes /usr/local/tomcat/shared/classes

ENV JAVA_OPTS "-server -Ddir.root=/usr/local/tomcat/data -XX:-DisableExplicitGC -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Dfile.encoding=UTF-8"

WORKDIR /root

VOLUME ["/usr/local/tomcat/data"]
