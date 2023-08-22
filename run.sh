#!/bin/bash


echo -e "\e[0m888                 \e[32m.d8888b.  8888888b.   .d8888b. \e[0m" 
echo -e "888                \e[32md88P  Y88b 888   Y88b d88P  Y88b\e[0m" 
echo -e "888                \e[32m888    888 888    888 888    888\e[0m" 
echo -e "88888b.   .d88b.   \e[32m888        888   d88P 888       \e[0m" 
echo -e "888 \"88b d8P  Y8b  \e[32m888        8888888P\"  888  88888\e[0m" 
echo -e "888  888 88888888  \e[32m888    888 888        888    888\e[0m" 
echo -e "888 d88P Y8b.      \e[32mY88b  d88P 888        Y88b  d88P\e[0m" 
echo -e "88888P\"   \"Y8888    \e[32m\"Y8888P\"  888         \"Y8888P88\e[0m" 
echo -e " \e[91mCopyright (C) 2010-2023 beCPG.\e[0m"


export COMPOSE_FILE_PATH=${PWD}/becpg-integration-runner/target/docker-compose.yml

export MVN_EXEC="${PWD}/mvnw"
export BECPG_VERSION_PROFILE=becpg_3_2_2

start() {
   	 	docker compose -p $BECPG_VERSION_PROFILE -f $COMPOSE_FILE_PATH -f docker-compose.override.yml up -d --remove-orphans
}

pull() {
   	 	docker compose -p $BECPG_VERSION_PROFILE -f $COMPOSE_FILE_PATH -f docker-compose.override.yml pull 
}

pull() {
   	 	docker compose -p $BECPG_VERSION_PROFILE -f $COMPOSE_FILE_PATH -f docker-compose.override.yml pull 
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
        docker compose -p $BECPG_VERSION_PROFILE -f $COMPOSE_FILE_PATH  -f docker-compose.override.yml down
    fi
}

deploy_fast(){

	#becpg-amp
	docker cp becpg-core/src/main/resources/alfresco/templates/. $BECPG_VERSION_PROFILE_becpg_1:/usr/local/tomcat/webapps/alfresco/WEB-INF/classes/alfresco/templates
	docker cp becpg-plm/becpg-plm-core/src/main/resources/alfresco/templates/. $BECPG_VERSION_PROFILE_becpg_1:/usr/local/tomcat/webapps/alfresco/WEB-INF/classes/alfresco/templates
	
	#becpg-share
	docker cp becpg-share/src/main/assembly/web/. $BECPG_VERSION_PROFILE_becpg_1:/usr/local/tomcat/webapps/share/
	docker cp becpg-share/src/main/resources/alfresco/. $BECPG_VERSION_PROFILE_becpg_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker cp becpg-designer/becpg-designer-share/src/main/assembly/web/. $BECPG_VERSION_PROFILE_becpg-share_1:/usr/local/tomcat/webapps/share/
	docker cp becpg-designer/becpg-designer-share/src/main/resources/alfresco/. $BECPG_VERSION_PROFILE_becpg-share_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker cp becpg-project/becpg-project-share/src/main/assembly/web/. $BECPG_VERSION_PROFILE_becpg-share_1:/usr/local/tomcat/webapps/share/
	docker cp becpg-project/becpg-project-share/src/main/resources/alfresco/. $BECPG_VERSION_PROFILE_becpg-share_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker cp becpg-plm/becpg-plm-share/src/main/assembly/web/. $BECPG_VERSION_PROFILE_becpg-share_1:/usr/local/tomcat/webapps/share/
	docker cp becpg-plm/becpg-plm-share/src/main/resources/alfresco/. $BECPG_VERSION_PROFILE_becpg-share_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker cp becpg-plm/becpg-plm-share/src/main/assembly/web/. $BECPG_VERSION_PROFILE_becpg-share_1:/usr/local/tomcat/webapps/share/
	docker cp becpg-plm/becpg-plm-share/src/main/assembly/config/alfresco/. $BECPG_VERSION_PROFILE_becpg-share_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	if [ -d becpg-enterprise ]; then
	  docker cp becpg-enterprise/becpg-enterprise-share/src/main/assembly/web/. $BECPG_VERSION_PROFILE_becpg-share_1:/usr/local/tomcat/webapps/share/
	  docker cp becpg-enterprise/becpg-enterprise-share/src/main/resources/alfresco/. $BECPG_VERSION_PROFILE_becpg-share_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	fi
	
	wget --delete-after --http-user=admin --http-password=becpg --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us --post-data reset=on http://localhost:8180/share/page/index

}

purge() {
    docker compose -p $BECPG_VERSION_PROFILE -f  $COMPOSE_FILE_PATH down -v
}

build() {
   if [ -d becpg-enterprise ]; then
    cd becpg-enterprise
   	 $MVN_EXEC  package $EXTRA_ENV -DskipTests=true -Dbecpg.dockerbuild.name="enterprise-test"
    cd ..
   else
   	 $MVN_EXEC  package $EXTRA_ENV -DskipTests=true -Dbecpg.dockerbuild.name="test"
   fi 
}

install() {
  if [ -d becpg-enterprise ]; then
    cd becpg-enterprise
    $MVN_EXEC  clean install $EXTRA_ENV -DskipTests=true -P full
     cd ..
   else
    $MVN_EXEC  clean install $EXTRA_ENV -DskipTests=true -P full
  fi
}

tail() {
    docker compose -p $BECPG_VERSION_PROFILE -f $COMPOSE_FILE_PATH logs -f --tail=100 becpg 
}

test() {
    $MVN_EXEC verify $EXTRA_ENV
}

reindex() {
	docker compose -p $BECPG_VERSION_PROFILE -f $COMPOSE_FILE_PATH -f docker-compose.override.yml stop solr
    docker compose  -p $BECPG_VERSION_PROFILE -f $COMPOSE_FILE_PATH -f docker-compose.override.yml rm -v solr
    docker volume rm $BECPG_VERSION_PROFILE_solr_data
	docker compose -p $BECPG_VERSION_PROFILE -f $COMPOSE_FILE_PATH -f docker-compose.override.yml up -d solr
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
  pull)
    pull
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
  reindex)
    reindex
    ;;
  visualvm)
    jvisualvm --openjmx localhost:9091
    ;;
  *)
    echo "Usage: $0 {install|build_start|build_test|start|stop|purge|tail|test|deploy_fast|visualvm|reindex}"
esac
