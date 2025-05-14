FROM ${docker.becpg.url}/becpg-base-share:${docker.becpg.tag} AS builder

ENV TOMCAT_DIR=/usr/local/tomcat

#Debuging 
#RUN curl -sL https://deb.nodesource.com/setup_10.x | bash - && apt-get install -y nodejs  && curl -L https://www.npmjs.com/install.sh | sh  && npm install -g less      
#COPY share/lessc-context.xml $TOMCAT_DIR/shared/classes/alfresco/web-extension
#COPY share/share-config-custom.xml $TOMCAT_DIR/shared/classes/alfresco/web-extension

COPY becpg-share-${project.version}.amp $TOMCAT_DIR/amps_share/
COPY becpg-project-share-${project.version}.amp $TOMCAT_DIR/amps_share/
COPY becpg-designer-share-${project.version}.amp $TOMCAT_DIR/amps_share/
COPY becpg-plm-share-${project.version}.amp $TOMCAT_DIR/amps_share/


RUN  java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps_share/becpg-share-${project.version}.amp webapps/share -nobackup -force && \
	 java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps_share/becpg-project-share-${project.version}.amp webapps/share -nobackup -force && \
	 java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps_share/becpg-designer-share-${project.version}.amp webapps/share -nobackup -force && \
	 java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps_share/becpg-plm-share-${project.version}.amp webapps/share -nobackup -force 
	 

FROM ${docker.becpg.url}/becpg-base-share:${docker.becpg.tag}

ENV TOMCAT_DIR=/usr/local/tomcat

         
COPY --chown=33000:1000 --from=builder ${TOMCAT_DIR}/webapps/share ${TOMCAT_DIR}/webapps/share
