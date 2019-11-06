FROM tomcat:9-jdk11
MAINTAINER beCPG

ENV NEXUS=https://artifacts.alfresco.com/nexus/content/groups/public
ENV ALF_VERSION=${alfresco.platform.version}
ENV VTI_VERSION=${alfresco.aos.version}
ENV ALF_SHARE_VERSION=${alfresco.share.version}
ENV MMT_VERSION=${alfresco.platform.version}
ENV PDF_RENDERER_VERSION=1.0
ENV MYSQL_CONNECTOR=${mysql.connector.version}
ENV SUPPORT_TOOLS_VERSION=1.1.0.0-amp

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
	mkdir -p /usr/local/tomcat/shared/classes


RUN set -x \
      && apt-get update \
      && apt-get install -y --no-install-recommends \
                  imagemagick \
                  ghostscript \
      && apt-get clean \
      && rm -rf /var/lib/apt/lists/*
      
WORKDIR /usr/local/tomcat/
      

## JAR - ALFRESCO MMT
RUN set -x && \
    curl --silent --location \
      ${NEXUS}/org/alfresco/alfresco-mmt/${MMT_VERSION}/alfresco-mmt-${MMT_VERSION}.jar \
      -o /root/alfresco-mmt.jar && \
      mkdir /root/amp
      
## Alfresco pdf renderer      
RUN set -x && \
    curl --silent --location \
      ${NEXUS}/org/alfresco/alfresco-pdf-renderer/${PDF_RENDERER_VERSION}/alfresco-pdf-renderer-${PDF_RENDERER_VERSION}-linux.tgz \
      -o /opt/tools/alfresco-pdf-renderer-${PDF_RENDERER_VERSION}-linux.tgz && \
      tar xvfz /opt/tools/alfresco-pdf-renderer-${PDF_RENDERER_VERSION}-linux.tgz && \
      mv alfresco-pdf-renderer /opt/tools/ && \
      rm /opt/tools/alfresco-pdf-renderer-${PDF_RENDERER_VERSION}-linux.tgz

## ALFRESCO.WAR
RUN set -x && \
    curl --silent --location \
      ${NEXUS}/org/alfresco/alfresco-platform/${ALF_VERSION}/alfresco-platform-${ALF_VERSION}.war \
      -o alfresco-platform-${ALF_VERSION}.war && \
    unzip -q alfresco-platform-${ALF_VERSION}.war -d webapps/alfresco && \
    rm alfresco-platform-${ALF_VERSION}.war

## SHARE.WAR    
RUN set -x && \
    curl --silent --location \
      ${NEXUS}/org/alfresco/share/${ALF_SHARE_VERSION}/share-${ALF_SHARE_VERSION}.war \
      -o share-${ALF_SHARE_VERSION}.war && \
    unzip -q share-${ALF_SHARE_VERSION}.war -d webapps/share && \
    rm share-${ALF_SHARE_VERSION}.war
    

## JDBC - MYSQL
RUN set -x && \
    curl --silent --location \
      http://central.maven.org/maven2/mysql/mysql-connector-java/${MYSQL_CONNECTOR}/mysql-connector-java-${MYSQL_CONNECTOR}.jar \
	-o lib/mysql-connector-java-${MYSQL_CONNECTOR}.jar


## AMP - ALFRESCO SHARE SERVICE
RUN set -x && \
    curl --silent --location \
      ${NEXUS}/org/alfresco/alfresco-share-services/${ALF_SHARE_VERSION}/alfresco-share-services-${ALF_SHARE_VERSION}.amp \
      -o /root/amp/alfresco-share-services-${ALF_SHARE_VERSION}.amp && \
    java -jar /root/alfresco-mmt.jar install /root/amp/ webapps/alfresco -nobackup -directory && \
    rm /root/amp/alfresco-share-services-${ALF_SHARE_VERSION}.amp


#workflow-reporting-repo 
RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/workflow-reporting-repo-1.0.0.amp  \
      -o /root/amp/workflow-reporting-repo-1.0.0.amp && \
    java -jar /root/alfresco-mmt.jar install /root/amp/ webapps/alfresco -nobackup -directory && \
    rm /root/amp/workflow-reporting-repo-1.0.0.amp      

RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/workflow-reporting-share-1.0.0.amp  \
      -o /root/amp/workflow-reporting-share-1.0.0.amp && \
    java -jar /root/alfresco-mmt.jar install /root/amp/ webapps/share -nobackup -directory && \
    rm /root/amp/workflow-reporting-share-1.0.0.amp     
    

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
RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/share-login-switcher-1.5.jar  \
      -o webapps/share/WEB-INF/lib/share-login-switcher-1.5.jar    
      
RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/repo-login-switcher-1.5.jar  \
      -o webapps/alfresco/WEB-INF/lib/repo-login-switcher-1.5.jar          
    
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
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/_vti_bin-${VTI_VERSION}.war  \
      -o _vti_bin-${VTI_VERSION}.war && \
    unzip -q _vti_bin-${VTI_VERSION}.war -d webapps/_vti_bin && \
    rm _vti_bin-${VTI_VERSION}.war
        	        
RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/alfresco-aos-core-${VTI_VERSION}.amp  \
      -o /root/amp/alfresco-aos-core-${VTI_VERSION}.amp && \
    java -jar /root/alfresco-mmt.jar install /root/amp/ webapps/alfresco -nobackup -directory && \
    rm /root/amp/alfresco-aos-core-${VTI_VERSION}.amp


      
COPY tomcat/share.xml /usr/local/tomcat/conf/Catalina/localhost/
COPY tomcat/catalina.properties /usr/local/tomcat/conf/catalina.properties
COPY tomcat/server.xml /usr/local/tomcat/conf/server.xml
COPY tomcat/logging.properties /usr/local/tomcat/conf/logging.properties

COPY becpg/classes /usr/local/tomcat/shared/classes

ENV JAVA_OPTS "-server -Ddir.root=/usr/local/tomcat/data -Djava.awt.headless=true -XX:-DisableExplicitGC -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Dfile.encoding=UTF-8"

WORKDIR /root

VOLUME ["/usr/local/tomcat/data"]
