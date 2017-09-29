FROM tomcat:8.5-jre8
MAINTAINER beCPG

WORKDIR /usr/local/tomcat/

ARG VERSION

RUN  rm -rf /usr/share/doc \
                webapps/docs \
                webapps/examples \
                webapps/manager \
                webapps/host-manager
                
COPY target/becpg-report-${VERSION} /usr/local/tomcat/webapps/becpg-report                

WORKDIR /root