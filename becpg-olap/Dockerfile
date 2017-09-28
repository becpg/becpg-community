FROM tomcat:8.5-jre8
MAINTAINER beCPG

WORKDIR /usr/local/tomcat/

ARG VERSION

RUN  rm -rf /usr/share/doc \
                webapps/docs \
                webapps/examples \
                webapps/manager \
                webapps/host-manager \
                webapps/ROOT
                
RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/saiku-2.5.war  \
      -o saiku.war && \
    unzip -q saiku.war -d webapps/saiku && \
	rm saiku.war       
	
RUN set -x && \
    curl --silent --location \
      https://readonly:YLdy9W5v@www.becpg.fr/becpg-repo/releases/addons/saiku-ui-2.5.war  \
      -o saiku-ui.war && \
    unzip -q saiku-ui.war -d webapps/saiku-ui && \
	rm saiku-ui.war       	         
                
RUN set -x && \
    curl --silent --location \
      http://central.maven.org/maven2/mysql/mysql-connector-java/5.1.44/mysql-connector-java-5.1.44.jar \
	-o lib/mysql-connector-java-5.1.44.jar                
  
RUN mkdir -p /usr/local/tomcat/saiku-repository
                
COPY target/docker-distribution/docker/dist/saiku  webapps/saiku
COPY target/docker-distribution/docker/dist/saiku-ui  webapps/saiku-ui
COPY src/main/tomcat/saiku.xml /usr/local/tomcat/conf/Catalina/localhost/
           
            

WORKDIR /root