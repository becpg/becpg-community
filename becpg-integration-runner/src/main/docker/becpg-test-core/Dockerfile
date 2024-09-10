FROM ${docker.becpg.url}/becpg-base-core:${docker.becpg.tag} AS becpg-base-test-core

ENV TOMCAT_DIR=/usr/local/tomcat

# HOTSWAP AGENT
# Install and configure Trava OpenJDK (OpenJDK pre-built with DCEVM and hotswap agent for Java 11)
RUN curl -sL  https://github.com/TravaOpenJDK/trava-jdk-11-dcevm-newgen/releases/download/dcevm-11.0.10%2B6/java11-openjdk-dcevm-linux-amd64.tar.gz \
      -o $TOMCAT_DIR/trava-jdk-11-dcevm.tar.gz && \
    mkdir -p /usr/java && tar -xvf $TOMCAT_DIR/trava-jdk-11-dcevm.tar.gz -C /usr/java/ && \
    rm $TOMCAT_DIR/trava-jdk-11-dcevm.tar.gz && \
    update-alternatives --install /usr/bin/java java /usr/java/dcevm-11.0.10+5/bin/java 40000 && \
    update-alternatives --install /usr/bin/javac javac /usr/java/dcevm-11.0.10+5/bin/javac 40000 && \
    update-alternatives --install /usr/bin/jar jar /usr/java/dcevm-11.0.10+5/bin/jar 40000 && \
    update-alternatives --set java /usr/java/dcevm-11.0.10+5/bin/java && \
    update-alternatives --set javac /usr/java/dcevm-11.0.10+5/bin/javac && \
    update-alternatives --set jar /usr/java/dcevm-11.0.10+5/bin/jar && \
    ln -sfn /usr/java/dcevm-11.0.10+5 /usr/java/latest && \
    ln -sfn /usr/java/dcevm-11.0.10+5 /usr/java/default

#Jacoco Agent
RUN curl -sL https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.8/org.jacoco.agent-0.8.8-runtime.jar -o /root/jacoco-runtime.jar

FROM becpg-base-test-core


ENV JAVA_HOME=/usr/java/default
ENV PATH $JAVA_HOME/bin:$PATH



#Debuging 
COPY alfresco/log4j.properties $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes
COPY alfresco/hotswap-agent.properties $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes
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

