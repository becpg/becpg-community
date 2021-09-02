FROM ${docker.becpg.url}/becpg-base-share:${docker.becpg.tag}

#RUN curl -sL https://deb.nodesource.com/setup_10.x | bash - && apt-get install -y nodejs  && curl -L https://www.npmjs.com/install.sh | sh  && npm install -g less      

ENV TOMCAT_DIR=/usr/local/tomcat

ENV PATH $JAVA_HOME/bin:$PATH

COPY becpg-share-${project.version}.amp $TOMCAT_DIR/amps_share/
COPY becpg-project-share-${project.version}.amp $TOMCAT_DIR/amps_share/
COPY becpg-designer-share-${project.version}.amp $TOMCAT_DIR/amps_share/
COPY becpg-plm-share-${project.version}.amp $TOMCAT_DIR/amps_share/



RUN set -x && \
	 java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps_share/becpg-share-${project.version}.amp webapps/share -nobackup -force && \
	 java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps_share/becpg-project-share-${project.version}.amp webapps/share -nobackup -force && \
	 java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps_share/becpg-designer-share-${project.version}.amp webapps/share -nobackup -force && \
	 java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps_share/becpg-plm-share-${project.version}.amp webapps/share -nobackup -force 
	 
RUN rm -rf $TOMCAT_DIR/amps_share/*

#Debuging 

#COPY share/hotswap-agent.properties $TOMCAT_DIR/webapps/share/WEB-INF/classes/hotswap-agent.properties
#COPY share/lessc-context.xml $TOMCAT_DIR/shared/classes/alfresco/web-extension
#COPY share/share-config-custom.xml $TOMCAT_DIR/shared/classes/alfresco/web-extension



