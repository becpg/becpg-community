FROM tomcat:8.5-jre8
MAINTAINER beCPG

ENV NEXUS=https://artifacts.alfresco.com/nexus/content/groups/public
ENV ALF_VERSION=${alfresco.version}
ENV ALF_SHARE_VERSION=${alfresco.share.version}
ENV MMT_VERSION=${alfresco.version}
ENV PDF_RENDERER_VERSION=1.0
ENV MYSQL_CONNECTOR=${mysql.connector.version}

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
      

#Add JAI and ImageIO for great speedy speed.
WORKDIR /tmp
# A little logic that will fetch the JAI and JAI ImageIO tar file if it
# is not available locally in the resources dir
RUN if [ ! -f /tmp/resources/jai-1_1_3-lib-linux-amd64.tar.gz ]; then \
    wget http://download.java.net/media/jai/builds/release/1_1_3/jai-1_1_3-lib-linux-amd64.tar.gz -P ./resources;\
    fi; \
    if [ ! -f /tmp/resources/jai_imageio-1_1-lib-linux-amd64.tar.gz ]; then \
    wget http://download.java.net/media/jai-imageio/builds/release/1.1/jai_imageio-1_1-lib-linux-amd64.tar.gz -P ./resources;\
    fi; \
    mv resources/jai-1_1_3-lib-linux-amd64.tar.gz ./ && \
    mv resources/jai_imageio-1_1-lib-linux-amd64.tar.gz ./ && \
    gunzip -c jai-1_1_3-lib-linux-amd64.tar.gz | tar xf - && \
    gunzip -c jai_imageio-1_1-lib-linux-amd64.tar.gz | tar xf - && \
    mv /tmp/jai-1_1_3/lib/*.jar $JAVA_HOME/lib/ext/ && \
    mv /tmp/jai-1_1_3/lib/*.so $JAVA_HOME/lib/amd64/ && \
    mv /tmp/jai_imageio-1_1/lib/*.jar $JAVA_HOME/lib/ext/ && \
    mv /tmp/jai_imageio-1_1/lib/*.so $JAVA_HOME/lib/amd64/ && \
    rm /tmp/jai-1_1_3-lib-linux-amd64.tar.gz && \
    rm -r /tmp/jai-1_1_3 && \
    rm /tmp/jai_imageio-1_1-lib-linux-amd64.tar.gz && \
    rm -r /tmp/jai_imageio-1_1
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


      
COPY tomcat/share.xml /usr/local/tomcat/conf/Catalina/localhost/
COPY tomcat/catalina.properties /usr/local/tomcat/conf/catalina.properties
COPY tomcat/server.xml /usr/local/tomcat/conf/server.xml
COPY tomcat/logging.properties /usr/local/tomcat/conf/logging.properties

COPY becpg/classes /usr/local/tomcat/shared/classes

ENV JAVA_OPTS "-server -Ddir.root=/usr/local/tomcat/data -Djava.awt.headless=true -XX:-DisableExplicitGC -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Dfile.encoding=UTF-8"

WORKDIR /root

VOLUME ["/usr/local/tomcat/data"]
