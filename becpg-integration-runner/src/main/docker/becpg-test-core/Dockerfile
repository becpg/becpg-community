FROM ${docker.becpg.url}/becpg-base-core:${docker.becpg.tag} AS builder

ENV TOMCAT_DIR=/usr/local/tomcat

# Copy all AMP files at once
COPY becpg-*-${project.version}.amp $TOMCAT_DIR/amps/

# Install all AMPs in a single RUN command
RUN for amp in $TOMCAT_DIR/amps/*.amp; do \
        java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $amp webapps/alfresco -nobackup -force; \
    done && \
    rm -rf $TOMCAT_DIR/amps/*

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

# Copy built artifacts from builder stage
COPY --chown=33000:1000 --from=builder ${TOMCAT_DIR}/webapps/alfresco/ ${TOMCAT_DIR}/webapps/alfresco/

# Copy extensions and additional resources
COPY --chown=33000:1000 extensions/*.jar $TOMCAT_DIR/webapps/alfresco/WEB-INF/lib/
COPY --chown=33000:1000 forms/ $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes/beCPG/forms/
COPY --chown=33000:1000 becpg-plm/ $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes/alfresco/module/becpg-plm-core/

