#!/bin/bash


echo -e "\e[0m888                 \e[32m.d8888b.  8888888b.   .d8888b. \e[0m" 
echo -e "888                \e[32md88P  Y88b 888   Y88b d88P  Y88b\e[0m" 
echo -e "888                \e[32m888    888 888    888 888    888\e[0m" 
echo -e "88888b.   .d88b.   \e[32m888        888   d88P 888       \e[0m" 
echo -e "888 \"88b d8P  Y8b  \e[32m888        8888888P\"  888  88888\e[0m" 
echo -e "888  888 88888888  \e[32m888    888 888        888    888\e[0m" 
echo -e "888 d88P Y8b.      \e[32mY88b  d88P 888        Y88b  d88P\e[0m" 
echo -e "88888P\"   \"Y8888    \e[32m\"Y8888P\"  888         \"Y8888P88\e[0m" 
echo -e " \e[91mCopyright (C) 2010-2020 beCPG.\e[0m"


export COMPOSE_FILE_PATH=${PWD}/becpg-integration-runner/target/docker-compose.yml

export MVN_EXEC="${PWD}/mvnw"

start() {
   	 	docker-compose -f $COMPOSE_FILE_PATH up -d --remove-orphans
}

down() {
	if [ -d becpg-enterprise ]; then
    cd becpg-enterprise
   	 $MVN_EXEC clean validate $EXTRA_ENV -DskipTests=true -Dbecpg.dockerbuild.name="enterprise-test"
    cd ..
   	else
   	 $MVN_EXEC clean validate $EXTRA_ENV -DskipTests=true -Dbecpg.dockerbuild.name="test"
    fi 
    if [ -f $COMPOSE_FILE_PATH ]; then
        docker-compose -f $COMPOSE_FILE_PATH down
    fi
}

deploy_fast(){

	#becpg-amp
	docker cp becpg-core/src/main/resources/alfresco/templates/. target_becpg_1:/usr/local/tomcat/webapps/alfresco/WEB-INF/classes/alfresco/templates
	
	#becpg-share
	docker cp becpg-share/src/main/assembly/web/. target_becpg_1:/usr/local/tomcat/webapps/share/
	docker cp becpg-share/src/main/resources/alfresco/. target_becpg_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker cp becpg-designer/becpg-designer-share/src/main/assembly/web/. target_becpg_1:/usr/local/tomcat/webapps/share/
	docker cp becpg-designer/becpg-designer-share/src/main/resources/alfresco/. target_becpg_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker cp becpg-project/becpg-project-share/src/main/assembly/web/. target_becpg_1:/usr/local/tomcat/webapps/share/
	docker cp becpg-project/becpg-project-share/src/main/resources/alfresco/. target_becpg_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker cp becpg-plm/becpg-plm-share/src/main/assembly/web/. target_becpg_1:/usr/local/tomcat/webapps/share/
	docker cp becpg-plm/becpg-plm-share/src/main/resources/alfresco/. target_becpg_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker cp becpg-plm/becpg-plm-share/src/main/assembly/web/. target_becpg_1:/usr/local/tomcat/webapps/share/
	docker cp becpg-plm/becpg-plm-share/src/main/assembly/config/alfresco/. target_becpg_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	if [ -d becpg-enterprise ]; then
	  docker cp becpg-enterprise/becpg-enterprise-share/src/main/assembly/web/. target_becpg_1:/usr/local/tomcat/webapps/share/
	  docker cp becpg-enterprise/becpg-enterprise-share/src/main/resources/alfresco/. target_becpg_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	fi
	
	wget --delete-after --http-user=admin --http-password=becpg --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us --post-data reset=on http://localhost:8080/share/page/index

}

purge() {
    docker-compose -f  $COMPOSE_FILE_PATH down -v
}

build() {
   if [ -d becpg-enterprise ]; then
    cd becpg-enterprise
   	 $MVN_EXEC  package $EXTRA_ENV -DskipTests=true  -Dbecpg.dockerbuild.name="enterprise-test"
    cd ..
   else
   	 $MVN_EXEC  package $EXTRA_ENV -DskipTests=true -Dbecpg.dockerbuild.name="test"
   fi 
}

install() {
    $MVN_EXEC  install $EXTRA_ENV -DskipTests=true -P full
}

tail() {
    docker-compose -f $COMPOSE_FILE_PATH logs -f --tail=50 becpg
}

test() {
    $MVN_EXEC verify $EXTRA_ENV -pl becpg-integration-runner
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
  visualvm)
    jvisualvm --openjmx localhost:9091
    ;;
  *)
    echo "Usage: $0 {install|build_start|build_test|start|stop|purge|tail|test|deploy_fast|visualvm}"
esac
