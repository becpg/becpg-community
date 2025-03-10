FROM ${docker.becpg.url}/becpg-base-core:${docker.becpg.tag} AS becpg-base-test-core

ENV TOMCAT_DIR=/usr/local/tomcat


#Jacoco Agent
RUN curl -sL https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.12/org.jacoco.agent-0.8.12-runtime.jar -o $TOMCAT_DIR/jacoco-runtime.jar

FROM becpg-base-test-core


#Debuging 
COPY alfresco/hotswap-agent.properties $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes
COPY alfresco/disable-hotswap-agent.properties $TOMCAT_DIR/webapps/ROOT/WEB-INF/classes/hotswap-agent.properties
COPY alfresco/disable-hotswap-agent.properties $TOMCAT_DIR/webapps/_vti_bin/WEB-INF/classes/hotswap-agent.properties
COPY alfresco/disable-hotswap-agent.properties $TOMCAT_DIR/webapps/api-explorer/WEB-INF/classes/hotswap-agent.properties

COPY alfresco/beCPG.properties $TOMCAT_DIR/shared/classes/beCPG.properties
COPY alfresco/alfresco-global.properties $TOMCAT_DIR/shared/classes/alfresco-global.properties
COPY alfresco/catalina.policy $TOMCAT_DIR/conf

      
COPY becpg-core-${project.version}.amp $TOMCAT_DIR/amps/
COPY becpg-project-core-${project.version}.amp  $TOMCAT_DIR/amps/
COPY becpg-designer-core-${project.version}.amp  $TOMCAT_DIR/amps/
COPY becpg-plm-core-${project.version}.amp  $TOMCAT_DIR/amps/

RUN  java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install  $TOMCAT_DIR/amps/becpg-core-${project.version}.amp webapps/alfresco -nobackup -force && \
	 java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install  $TOMCAT_DIR/amps/becpg-project-core-${project.version}.amp webapps/alfresco -nobackup -force && \
	 java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install  $TOMCAT_DIR/amps/becpg-designer-core-${project.version}.amp webapps/alfresco -nobackup -force && \
	 java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install  $TOMCAT_DIR/amps/becpg-plm-core-${project.version}.amp webapps/alfresco -nobackup -force 
	 
RUN rm -rf  $TOMCAT_DIR/amps/*

COPY extensions/*.jar $TOMCAT_DIR/webapps/alfresco/WEB-INF/lib/

COPY forms $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes/beCPG/forms/
COPY becpg-plm $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes/alfresco/module/becpg-plm-core/

