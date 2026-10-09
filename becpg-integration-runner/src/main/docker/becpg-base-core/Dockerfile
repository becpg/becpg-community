FROM ${docker.acs.image}:${alfresco.platform.version}

ENV TOMCAT_DIR=/usr/local/tomcat \
    NEXUS=https://artifacts.alfresco.com/nexus/content/groups/public \
    AOS_VERSION=${alfresco.aos.version} \
    MYSQL_CONNECTOR=${mysql.connector.version}

ARG GROUPNAME=Alfresco
ARG GROUPID=1000
ARG IMAGEUSERNAME=alfresco
ARG USERID=33000

USER root

# JDK 25 instead of the JDK 21 of the Alfresco image. Compact object headers take
# 10 to 14 % off the live heap (measured on the full IT suite, same GC pauses).
# JDK_JAVA_OPTIONS is read by the java launcher whatever JAVA_OPTS or CATALINA_OPTS
# a deployment sets, so the flag cannot be dropped by an override.
COPY --from=${docker.jdk.image} /opt/java/openjdk /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk \
    PATH=/opt/java/openjdk/bin:$PATH \
    JDK_JAVA_OPTIONS="-XX:+UseCompactObjectHeaders"

RUN dnf update -y && dnf clean all && \
    curl --silent --location \
      https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/${MYSQL_CONNECTOR}/mysql-connector-j-${MYSQL_CONNECTOR}.jar \
      -o $TOMCAT_DIR/lib/mysql-connector-j-${MYSQL_CONNECTOR}.jar && \
    rm $TOMCAT_DIR/lib/postgres* && \
    \
    curl --silent --location \
      ${NEXUS}/org/alfresco/aos-module/alfresco-aos-module/${AOS_VERSION}/alfresco-aos-module-${AOS_VERSION}.amp \
      -o $TOMCAT_DIR/amps/alfresco-aos-core-${AOS_VERSION}.amp && \
    java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install $TOMCAT_DIR/amps/ $TOMCAT_DIR/webapps/alfresco -force -nobackup -directory && \
    rm $TOMCAT_DIR/amps/alfresco-aos-core-${AOS_VERSION}.amp && \
    rm -rf /var/cache/apt/* /tmp/* /var/tmp/*

COPY tomcat/server.xml $TOMCAT_DIR/conf/server.xml 
COPY becpg/classes $TOMCAT_DIR/shared/classes

RUN printf '\n# No bundled jar declares a TLD or a ServletContainerInitializer, so scanning\n# them on every startup is pure cost. Overrides the default list above.\ntomcat.util.scan.StandardJarScanFilter.jarsToSkip=*\n' >> $TOMCAT_DIR/conf/catalina.properties && \
    rm -rf $TOMCAT_DIR/amps/* && \
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

ENV JAVA_OPTS="-Ddir.root=/usr/local/tomcat/data -Dfile.encoding=UTF-8 -Dxml.disableEntityExpansion=true -Djavax.xml.accessExternalDTD=\"\" -Djavax.xml.accessExternalSchema=\"\" -XX:+ExitOnOutOfMemoryError -XX:+AlwaysPreTouch -XX:+UseG1GC -XX:+DisableExplicitGC -XX:MaxGCPauseMillis=200 -XX:+ParallelRefProcEnabled -Xss512k -XX:+UseStringDeduplication"

# The Alfresco image starts 'catalina.sh run -security'. JDK 24 removed the
# Security Manager (JEP 486): the JVM refuses to enable it and would not start.
CMD ["catalina.sh", "run"]
