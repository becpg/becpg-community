FROM ${docker.acs.image}:${alfresco.platform.version}

ENV TOMCAT_DIR=/usr/local/tomcat \
    NEXUS=https://artifacts.alfresco.com/nexus/content/groups/public \
    AOS_VERSION=${alfresco.aos.version} \
    MYSQL_CONNECTOR=${mysql.connector.version} \
    SUPPORT_TOOLS_VERSION=${becpg.supporttools.version} \
    I18_VERSION=${becpg.i18n.version}

ARG GROUPNAME=Alfresco
ARG GROUPID=1000
ARG IMAGEUSERNAME=alfresco
ARG USERID=33000

USER root

RUN curl --silent --location \
      https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/${MYSQL_CONNECTOR}/mysql-connector-j-${MYSQL_CONNECTOR}.jar \
      -o $TOMCAT_DIR/lib/mysql-connector-j-${MYSQL_CONNECTOR}.jar && \
    rm $TOMCAT_DIR/lib/postgres* && \
    \
    curl --silent --location \
      https://storage.googleapis.com/becpg-repo/addons/support-tools-repo-${SUPPORT_TOOLS_VERSION}.amp \
      -o $TOMCAT_DIR/amps/support-tools-repo-${SUPPORT_TOOLS_VERSION}.amp && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps/ $TOMCAT_DIR/webapps/alfresco -nobackup -directory && \
    rm $TOMCAT_DIR/amps/support-tools-repo-${SUPPORT_TOOLS_VERSION}.amp && \
    \
    curl --silent --location \
      ${NEXUS}/org/alfresco/aos-module/alfresco-aos-module/${AOS_VERSION}/alfresco-aos-module-${AOS_VERSION}.amp \
      -o $TOMCAT_DIR/amps/alfresco-aos-core-${AOS_VERSION}.amp && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps/ $TOMCAT_DIR/webapps/alfresco -force -nobackup -directory && \
    rm $TOMCAT_DIR/amps/alfresco-aos-core-${AOS_VERSION}.amp && \
    \
    curl --silent --location \
      https://storage.googleapis.com/becpg-repo/addons/alfresco-becpg-i18n-${I18_VERSION}.jar \
      -o $TOMCAT_DIR/webapps/alfresco/WEB-INF/lib/alfresco-becpg-i18n-${I18_VERSION}.jar && \
    rm -rf /var/cache/apt/* /tmp/* /var/tmp/*

COPY tomcat/server.xml $TOMCAT_DIR/conf/server.xml 
COPY becpg/classes $TOMCAT_DIR/shared/classes

RUN rm -rf $TOMCAT_DIR/amps/* && \
    mkdir -p ${TOMCAT_DIR}/data && \
    rm -rf $TOMCAT_DIR/alf_data && \
    \
    chgrp -R ${GROUPNAME} ${TOMCAT_DIR} && \
    chmod g+rx ${TOMCAT_DIR}/conf && \
    chmod -R g+r ${TOMCAT_DIR}/conf && \
    find ${TOMCAT_DIR}/webapps -type d -exec chmod 0750 {} \; && \
    find ${TOMCAT_DIR}/webapps -type f -exec chmod 0640 {} \; && \
    chmod -R g+r ${TOMCAT_DIR}/webapps && \
    chmod g+r ${TOMCAT_DIR}/conf/Catalina && \
    chmod g+rwx ${TOMCAT_DIR}/data && \
    chmod g+rwx ${TOMCAT_DIR}/logs && \
    chmod o-w ${TOMCAT_DIR}/logs && \
    chmod g+rwx ${TOMCAT_DIR}/temp && \
    chmod g+rwx ${TOMCAT_DIR}/work && \
    chmod o-w ${TOMCAT_DIR}/work && \
    chmod 664 ${TOMCAT_DIR}/alfresco-mmt/alfresco-mmt-*.jar && \
    find /licenses -type d -exec chmod 0755 {} \; && \
    find /licenses -type f -exec chmod 0644 {} \;

ENV JAVA_OPTS="-Ddir.root=/usr/local/tomcat/data -XX:+ExitOnOutOfMemoryError  -XX:+AlwaysPreTouch -XX:+UseG1GC -XX:+DisableExplicitGC -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+ParallelRefProcEnabled -Xss512k -XX:+UseStringDeduplication"

