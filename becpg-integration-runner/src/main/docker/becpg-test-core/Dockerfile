FROM ${docker.becpg.url}/becpg-base-core:${docker.becpg.tag} AS builder

ENV TOMCAT_DIR=/usr/local/tomcat

      
COPY becpg-core-${project.version}.amp $TOMCAT_DIR/amps/
COPY becpg-project-core-${project.version}.amp  $TOMCAT_DIR/amps/
COPY becpg-designer-core-${project.version}.amp  $TOMCAT_DIR/amps/
COPY becpg-plm-core-${project.version}.amp  $TOMCAT_DIR/amps/

RUN  java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install  $TOMCAT_DIR/amps/becpg-core-${project.version}.amp webapps/alfresco -nobackup -force && \
	 java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install  $TOMCAT_DIR/amps/becpg-project-core-${project.version}.amp webapps/alfresco -nobackup -force && \
	 java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install  $TOMCAT_DIR/amps/becpg-designer-core-${project.version}.amp webapps/alfresco -nobackup -force && \
	 java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install  $TOMCAT_DIR/amps/becpg-plm-core-${project.version}.amp webapps/alfresco -nobackup -force 
	 
FROM ${docker.becpg.url}/becpg-base-core:${docker.becpg.tag}

ENV TOMCAT_DIR=/usr/local/tomcat

# Download Jacoco Agent
RUN curl -sL https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.12/org.jacoco.agent-0.8.12-runtime.jar \
    -o $TOMCAT_DIR/jacoco-runtime.jar

# Copy hotswap configurations
COPY --chown=33000:1000 alfresco/hotswap-agent.properties $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes/
COPY --chown=33000:1000 alfresco/disable-hotswap-agent.properties $TOMCAT_DIR/webapps/ROOT/WEB-INF/classes/hotswap-agent.properties
COPY --chown=33000:1000 alfresco/disable-hotswap-agent.properties $TOMCAT_DIR/webapps/_vti_bin/WEB-INF/classes/hotswap-agent.properties
COPY --chown=33000:1000 alfresco/disable-hotswap-agent.properties $TOMCAT_DIR/webapps/api-explorer/WEB-INF/classes/hotswap-agent.properties

# Copy configuration files
COPY --chown=33000:1000 alfresco/beCPG.properties $TOMCAT_DIR/shared/classes/
COPY --chown=33000:1000 alfresco/alfresco-global.properties $TOMCAT_DIR/shared/classes/
COPY --chown=33000:1000 alfresco/catalina.policy $TOMCAT_DIR/conf/
COPY --chown=33000:1000 alfresco/custom-rendition-config.json $TOMCAT_DIR/shared/classes/alfresco/extension/transform/renditions/

# Copy built artifacts from builder stage
COPY --chown=33000:1000 --from=builder ${TOMCAT_DIR}/webapps/alfresco/ ${TOMCAT_DIR}/webapps/alfresco/

# Copy extensions and additional resources
COPY --chown=33000:1000 extensions/*.jar $TOMCAT_DIR/webapps/alfresco/WEB-INF/lib/
COPY --chown=33000:1000 forms/ $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes/beCPG/forms/
COPY --chown=33000:1000 becpg-plm/ $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes/alfresco/module/becpg-plm-core/

