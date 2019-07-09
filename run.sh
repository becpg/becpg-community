#!/bin/bash


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
	docker cp becpg-core/src/main/resources/alfresco/templates/. becpg:/usr/local/tomcat/webapps/alfresco/WEB-INF/classes/alfresco/templates
	
	#becpg-share
	docker cp becpg-share/src/main/assembly/web/. target_becpg_1:/usr/local/tomcat/webapps/share/
	docker cp becpg-share/src/main/resources/alfresco/. target_becpg_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker cp becpg-designer/becpg-designer-share/src/main/assembly/web/. target_becpg_1:/usr/local/tomcat/webapps/share/
	docker cp becpg-designer/becpg-designer-share/src/main/resources/alfresco/. target_becpg_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker cp becpg-project/becpg-project-share/src/main/assembly/web/. target_becpg_1:/usr/local/tomcat/webapps/share/
	docker cp becpg-project/becpg-project-share/src/main/resources/alfresco/. target_becpg_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker cp becpg-plm/becpg-plm-share/src/main/assembly/web/. target_becpg_1:/usr/local/tomcat/webapps/share/
	docker cp becpg-plm/becpg-plm-share/src/main/resources/alfresco/. target_becpg_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	if [ -d becpg-enterprise ]; then
	  docker cp becpg-enterprise/becpg-enterprise-share/src/main/assembly/web/. target_becpg_1:/usr/local/tomcat/webapps/share/
	  docker cp becpg-enterprise/becpg-enterprise-share/src/main/resources/alfresco/. target_becpg_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	fi
	
	#wget --delete-after --http-user=admin --http-password=becpg --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us --post-data reset=on http://localhost:8080/share/page/index

}

purge() {
    docker-compose -f  $COMPOSE_FILE_PATH down -v
}

build() {
    $MVN_EXEC clean package $EXTRA_ENV -DskipTests=true -P build
}

install() {
    $MVN_EXEC clean install $EXTRA_ENV -DskipTests=true -P full
}

tail() {
    docker-compose -f $COMPOSE_FILE_PATH logs -f --tail=50
}

test() {
    $MVN_EXEC verify $EXTRA_ENV -pl becpg-integration-runner
}

test_jenkins() {
    $MVN_EXEC verify -o $EXTRA_ENV
}

case "$1" in
  install)
    install
    ;;
  build_start)
    build
    start
    tail
    ;;
  build_test)
    build
    start
    test
    tail
    down
    ;;
  start)
    start
    tail
    ;;
  stop)
    down
    ;;
  deploy_fast)
    deploy_fast
    ;;  
  purge)
    down
    purge
    ;;
  tail)
    tail
    ;;
  test)
    test
    ;;
  *)
    echo "Usage: $0 {install|build_start|build_test|start|stop|purge|tail|test|deploy_fast}"
esac