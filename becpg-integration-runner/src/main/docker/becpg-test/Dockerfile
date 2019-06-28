FROM ${docker.becpg.url}/becpg-base:${project.version}
MAINTAINER beCPG

RUN curl -sL https://deb.nodesource.com/setup_9.x | bash - && apt-get install -y nodejs  && curl -L https://www.npmjs.com/install.sh | sh  && npm install -g less      

WORKDIR /usr/local/tomcat/
ENV TOMCAT_DIR=/usr/local/tomcat/

# HOTSWAP AGENT
# Install and configure Trava OpenJDK (OpenJDK pre-built with DCEVM and hotswap agent for Java 11)
#RUN curl -sL  https://github.com/TravaOpenJDK/trava-jdk-11-dcevm/releases/download/dcevm-11.0.1%2B8/java11-openjdk-dcevm-linux.tar.gz \
#      -o $TOMCAT_DIR/trava-jdk-11-dcevm.tar.gz
#RUN mkdir -p /usr/java && tar -xvf $TOMCAT_DIR/trava-jdk-11-dcevm.tar.gz -C /usr/java/ && \
#    rm $TOMCAT_DIR/trava-jdk-11-dcevm.tar.gz && \
#    alternatives --install /usr/bin/java java /usr/java/dcevm-11.0.1+7/bin/java 40000 && \
#    alternatives --install /usr/bin/javac javac /usr/java/dcevm-11.0.1+7/bin/javac 40000 && \
#    alternatives --install /usr/bin/jar jar /usr/java/dcevm-11.0.1+7/bin/jar 40000 && \
#    alternatives --set java /usr/java/dcevm-11.0.1+7/bin/java && \
#    alternatives --set javac /usr/java/dcevm-11.0.1+7/bin/javac && \
#    alternatives --set jar /usr/java/dcevm-11.0.1+7/bin/jar && \
#    ln -sfn /usr/java/dcevm-11.0.1+7 /usr/java/latest && \
#    ln -sfn /usr/java/dcevm-11.0.1+7 /usr/java/default

      
COPY becpg-core-${project.version}.amp /root/amp/
COPY becpg-project-core-${project.version}.amp /root/amp/
COPY becpg-designer-core-${project.version}.amp /root/amp/
COPY becpg-plm-core-${project.version}.amp /root/amp/
COPY becpg-share-${project.version}.amp /root/amp/
COPY becpg-project-share-${project.version}.amp /root/amp/
COPY becpg-designer-share-${project.version}.amp /root/amp/
COPY becpg-plm-share-${project.version}.amp /root/amp/


RUN set -x && \
	 java -jar /root/alfresco-mmt.jar install /root/amp/becpg-core-${project.version}.amp webapps/alfresco -nobackup -force && \
	 java -jar /root/alfresco-mmt.jar install /root/amp/becpg-project-core-${project.version}.amp webapps/alfresco -nobackup -force && \
	 java -jar /root/alfresco-mmt.jar install /root/amp/becpg-designer-core-${project.version}.amp webapps/alfresco -nobackup -force && \
	 java -jar /root/alfresco-mmt.jar install /root/amp/becpg-plm-core-${project.version}.amp webapps/alfresco -nobackup -force && \
	 java -jar /root/alfresco-mmt.jar install /root/amp/becpg-share-${project.version}.amp webapps/share -nobackup -force && \
	 java -jar /root/alfresco-mmt.jar install /root/amp/becpg-project-share-${project.version}.amp webapps/share -nobackup -force && \
	 java -jar /root/alfresco-mmt.jar install /root/amp/becpg-designer-share-${project.version}.amp webapps/share -nobackup -force && \
	 java -jar /root/alfresco-mmt.jar install /root/amp/becpg-plm-share-${project.version}.amp webapps/share -nobackup -force 
	 
RUN rm -rf /root/amp/*

#Debuging 
#COPY share-config-custom.xml $TOMCAT_DIR/shared/classes/alfresco/web-extension
COPY log4j.properties $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes
COPY hotswap-agent.properties $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes
COPY beCPG.properties $TOMCAT_DIR/shared/classes/beCPG.properties
COPY alfresco-global.properties $TOMCAT_DIR/shared/classes/alfresco-global.properties

COPY disable-webscript-caching-context.xml $TOMCAT_DIR/shared/classes/alfresco/extension
COPY lessc-context.xml $TOMCAT_DIR/shared/classes/alfresco/web-extension

# Copy Dockerfile to avoid an error if no JARs exist
COPY extensions/*.jar $TOMCAT_DIR/webapps/alfresco/WEB-INF/lib/

WORKDIR /root

