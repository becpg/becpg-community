FROM ${docker.becpg.url}/becpg-base-core:${docker.becpg.tag} AS becpg-base-test-core

ENV TOMCAT_DIR=/usr/local/tomcat

# HOTSWAP AGENT
# Install and configure Trava OpenJDK (OpenJDK pre-built with DCEVM and hotswap agent for Java 11)
RUN curl -sL  https://cache-redirector.jetbrains.com/intellij-jbr/jbr_jcef-17.0.10-linux-x64-b1207.14.tar.gz \
      -o $TOMCAT_DIR/jbr_jcef-17.0.10-linux-x64-b1207.14.tar.gz && \
     mkdir -p /usr/java && tar -xvf $TOMCAT_DIR/jbr_jcef-17.0.10-linux-x64-b1207.14.tar.gz -C /usr/java/ && \
     rm $TOMCAT_DIR/jbr_jcef-17.0.10-linux-x64-b1207.14.tar.gz && \
    update-alternatives --install /usr/bin/java java /usr/java/jbr_jcef-17.0.10-linux-x64-b1207.14/bin/java 40000 && \
    update-alternatives --install /usr/bin/javac javac /usr/java/jbr_jcef-17.0.10-linux-x64-b1207.14/bin/javac 40000 && \
    update-alternatives --install /usr/bin/jar jar /usr/java/jbr_jcef-17.0.10-linux-x64-b1207.14/bin/jar 40000 && \
    update-alternatives --set java /usr/java/jbr_jcef-17.0.10-linux-x64-b1207.14/bin/java && \
    update-alternatives --set javac /usr/java/jbr_jcef-17.0.10-linux-x64-b1207.14/bin/javac && \
    update-alternatives --set jar /usr/java/jbr_jcef-17.0.10-linux-x64-b1207.14/bin/jar && \
    ln -sfn /usr/java/jbr_jcef-17.0.10-linux-x64-b1207.14 /usr/java/latest && \
    ln -sfn /usr/java/jbr_jcef-17.0.10-linux-x64-b1207.14 /usr/java/default
    
    
RUN  mkdir -p /usr/java/jbr_jcef-17.0.10-linux-x64-b1207.14/lib/hotswap/ && \
     curl -sL https://github.com/HotswapProjects/HotswapAgent/releases/download/1.4.2-SNAPSHOT/hotswap-agent-1.4.2-SNAPSHOT.jar -o  \
     /usr/java/jbr_jcef-17.0.10-linux-x64-b1207.14/lib/hotswap/hotswap-agent.jar
   
#Jacoco Agent
RUN curl -sL https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.11/org.jacoco.agent-0.8.11-runtime.jar -o $TOMCAT_DIR/jacoco-runtime.jar

FROM becpg-base-test-core


ENV JAVA_HOME=/usr/java/default
ENV PATH=$JAVA_HOME/bin:$PATH


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

