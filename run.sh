#!/bin/sh


echo -e "\e[0m888                 \e[32m.d8888b.  8888888b.   .d8888b. \e[0m" 
echo -e "888                \e[32md88P  Y88b 888   Y88b d88P  Y88b\e[0m" 
echo -e "888                \e[32m888    888 888    888 888    888\e[0m" 
echo -e "88888b.   .d88b.   \e[32m888        888   d88P 888       \e[0m" 
echo -e "888 \"88b d8P  Y8b  \e[32m888        8888888P\"  888  88888\e[0m" 
echo -e "888  888 88888888  \e[32m888    888 888        888    888\e[0m" 
echo -e "888 d88P Y8b.      \e[32mY88b  d88P 888        Y88b  d88P\e[0m" 
echo -e "88888P\"   \"Y8888    \e[32m\"Y8888P\"  888         \"Y8888P88\e[0m" 
echo -e " \e[91mCopyright (C) 2010-2019 beCPG.\e[0m"


export COMPOSE_FILE_PATH=${PWD}/becpg-integration-runner/target/docker-compose.yml

if [ -z "${M2_HOME}" ]; then
  export MVN_EXEC="mvn"
else
  export MVN_EXEC="${M2_HOME}/bin/mvn"
fi

start() {
    docker-compose -f $COMPOSE_FILE_PATH up -d
}

down() {
    if [ -f $COMPOSE_FILE_PATH ]; then
        docker-compose -f $COMPOSE_FILE_PATH down
    fi
}

deploy_fast(){

	#becpg-amp
	docker-compose cp becpg-core/src/main/amp/config/alfresco/templates/. becpg:/usr/local/tomcat/webapps/alfresco/WEB-INF/classes/alfresco/templates
	
	#becpg-share
	docker-compose cp becpg-share/src/main/amp/config/alfresco/module/becpg-share/web/. becpg:/usr/local/tomcat/webapps/share/
	docker-compose cp becpg-share/src/main/amp/config/alfresco/module/becpg-share/config/. becpg:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker-compose cp becpg-designer/becpg-designer-share/src/main/amp/config/alfresco/module/becpg-designer-share/web/. becpg:/usr/local/tomcat/webapps/share/
	docker-compose cp becpg-designer/becpg-designer-share/src/main/amp/config/alfresco/module/becpg-designer-share/config/. becpg:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker-compose cp becpg-project/becpg-project-share/src/main/amp/config/alfresco/module/becpg-project-share/web/. becpg:/usr/local/tomcat/webapps/share/
	docker-compose cp becpg-project/becpg-project-share/src/main/amp/config/alfresco/module/becpg-project-share/config/. becpg:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker-compose cp becpg-plm/becpg-plm-share/src/main/amp/config/alfresco/module/becpg-plm-share/web/. becpg:/usr/local/tomcat/webapps/share/
	docker-compose cp becpg-plm/becpg-plm-share/src/main/amp/config/alfresco/module/becpg-plm-share/config/. becpg:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	
	#docker cp becpg-enterprise/becpg-enterprise-share/src/main/amp/config/alfresco/module/becpg-enterprise-share/web/. $CONTAINER_PLM_NAME:/usr/local/tomcat/webapps/share/
	#docker cp becpg-enterprise/becpg-enterprise-share/src/main/amp/config/alfresco/module/becpg-enterprise-share/config/. $CONTAINER_PLM_NAME:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	
	wget --delete-after --http-user=admin --http-password=becpg --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us --post-data reset=on http://localhost:80/share/page/index

}

purge() {
    docker-compose -f  $COMPOSE_FILE_PATH down -v
}

build() {
    $MVN_EXEC clean package -DskipTests=true
}

build_full() {
    $MVN_EXEC clean package -DskipTests=true -P full
}

tail() {
    docker-compose -f $COMPOSE_FILE_PATH logs -f --tail=50 becpg
}

test() {
    $MVN_EXEC verify -pl becpg-integration-runner
}

case "$1" in
  build_full_start)
    build_full
    start
    tail
    ;;
  build_start)
    build
    start
    tail
    ;;
  start)
    start
    tail
    ;;
  stop)
    down
    ;;
  purge)
    down
    purge
    ;;
  tail)
    tail
    ;;
  build_test)
    build_full
    start
    test
    tail
    down
    ;;
  test)
    test
    ;;
  *)
    echo "Usage: $0 {build_start|start|stop|purge|tail|build_test|test}"
esac