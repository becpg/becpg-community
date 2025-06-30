#!/bin/bash


echo -e "\e[38;2;0;255;189m888                 \e[38;2;0;92;102m.d8888b.  8888888b.   .d8888b. \e[38;2;0;255;189m" 
echo -e "888                \e[38;2;0;92;102md88P  Y88b 888   Y88b d88P  Y88b\e[38;2;0;255;189m" 
echo -e "888                \e[38;2;0;92;102m888    888 888    888 888    888\e[38;2;0;255;189m" 
echo -e "88888b.   .d88b.   \e[38;2;0;92;102m888        888   d88P 888       \e[38;2;0;255;189m" 
echo -e "888 \"88b d8P  Y8b  \e[38;2;0;92;102m888        8888888P\"  888  88888\e[38;2;0;255;189m" 
echo -e "888  888 88888888  \e[38;2;0;92;102m888    888 888        888    888\e[38;2;0;255;189m" 
echo -e "888 d88P Y8b.      \e[38;2;0;92;102mY88b  d88P 888        Y88b  d88P\e[38;2;0;255;189m" 
echo -e "88888P\"   \"Y8888    \e[38;2;0;92;102m\"Y8888P\"  888         \"Y8888P88\e[0m" 
echo -e " \e[91mCopyright (C) 2010-2025 beCPG.\e[0m"

set -e

export COMPOSE_FILE_PATH=${PWD}/becpg-integration-runner/target/docker-compose.yml
export MVN_EXEC="${PWD}/mvnw"
export BECPG_VERSION_PROFILE=becpg_23_4_2

if [ -f .env ]; then
  . .env
else
  echo "Warning: .env file not found, skipping."
fi

case "$2" in
  branch)
    BECPG_VERSION_PROFILE=$(echo "$BECPG_VERSION_PROFILE" | sed 's/\//_/g; s/\./-/g')
    BRANCH_NAME=$(echo "$(git rev-parse --abbrev-ref HEAD)" | sed 's/\//_/g; s/\./-/g')
    export BECPG_VERSION_PROFILE="${BECPG_VERSION_PROFILE}_${BRANCH_NAME}"
    ;;
  *)
    ;;
esac

start() {
   	 	docker compose -p $BECPG_VERSION_PROFILE -f $COMPOSE_FILE_PATH -f docker-compose.override.yml up -d --remove-orphans
}

start_test() {
   	 	docker compose -p becpg_test -f $COMPOSE_FILE_PATH up -d --remove-orphans
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

down_test() {
	if [ -d becpg-enterprise ]; then
	    cd becpg-enterprise
	   	 $MVN_EXEC clean validate $EXTRA_ENV -DskipTests=true -Dbecpg.dockerbuild.name="enterprise-test"
	    cd ..
   	else
   	 $MVN_EXEC clean validate $EXTRA_ENV -DskipTests=true -Dbecpg.dockerbuild.name="test"
    fi 
    if [ -f $COMPOSE_FILE_PATH ]; then
        docker compose -p becpg_test -f $COMPOSE_FILE_PATH  down -v
    fi
}

deploy_fast(){

	#becpg-amp
	docker cp becpg-core/src/main/resources/alfresco/templates/. $BECPG_VERSION_PROFILE-becpg-1:/usr/local/tomcat/webapps/alfresco/WEB-INF/classes/alfresco/templates
	docker cp becpg-plm/becpg-plm-core/src/main/resources/alfresco/templates/. $BECPG_VERSION_PROFILE-becpg-1:/usr/local/tomcat/webapps/alfresco/WEB-INF/classes/alfresco/templates
	
	#becpg-share
	docker cp becpg-share/src/main/assembly/web/. $BECPG_VERSION_PROFILE-becpg-share-1:/usr/local/tomcat/webapps/share/
	docker cp becpg-share/src/main/resources/alfresco/. $BECPG_VERSION_PROFILE-becpg-share-1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker cp becpg-designer/becpg-designer-share/src/main/assembly/web/. $BECPG_VERSION_PROFILE-becpg-share-1:/usr/local/tomcat/webapps/share/
	docker cp becpg-designer/becpg-designer-share/src/main/resources/alfresco/. $BECPG_VERSION_PROFILE-becpg-share-1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker cp becpg-project/becpg-project-share/src/main/assembly/web/. $BECPG_VERSION_PROFILE-becpg-share-1:/usr/local/tomcat/webapps/share/
	docker cp becpg-project/becpg-project-share/src/main/resources/alfresco/. $BECPG_VERSION_PROFILE-becpg-share-1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker cp becpg-plm/becpg-plm-share/src/main/assembly/web/. $BECPG_VERSION_PROFILE-becpg-share-1:/usr/local/tomcat/webapps/share/
	docker cp becpg-plm/becpg-plm-share/src/main/resources/alfresco/. $BECPG_VERSION_PROFILE-becpg-share-1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	docker cp becpg-plm/becpg-plm-share/src/main/assembly/web/. $BECPG_VERSION_PROFILE-becpg-share-1:/usr/local/tomcat/webapps/share/
	docker cp becpg-plm/becpg-plm-share/src/main/assembly/config/alfresco/. $BECPG_VERSION_PROFILE-becpg-share-1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	if [ -d becpg-enterprise ]; then
	  docker cp becpg-enterprise/becpg-enterprise-share/src/main/assembly/web/. $BECPG_VERSION_PROFILE-becpg-share-1:/usr/local/tomcat/webapps/share/
	  docker cp becpg-enterprise/becpg-enterprise-share/src/main/resources/alfresco/. $BECPG_VERSION_PROFILE-becpg-share-1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/
	fi
	
	wget --delete-after --http-user=admin --http-password=becpg --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us --post-data reset=on http://localhost:8180/share/page/index

}

purge() {
    docker compose -p $BECPG_VERSION_PROFILE -f  $COMPOSE_FILE_PATH down -v
}

build() {
   if [ -d becpg-enterprise ]; then
    cd becpg-enterprise
  	 $MVN_EXEC package $EXTRA_ENV -DskipTests=true  -Dmaven.build.cache.enabled=true -Djacoco.skip=true -Dcheckstyle.skip=true  -Dbecpg.dockerbuild.name="enterprise-test"
     COMPOSE_FILE="./distribution/target/docker-compose-dev.yml"

      docker compose -f $COMPOSE_FILE build becpg-base-core
      docker compose -f $COMPOSE_FILE build becpg-base-share
      docker compose -f $COMPOSE_FILE build becpg-test-core
      docker compose -f $COMPOSE_FILE build becpg-test-share
      docker compose -f $COMPOSE_FILE build becpg-enterprise-test-core
      docker compose -f $COMPOSE_FILE build becpg-enterprise-test-share
   	 cd ..
   else
   	 $MVN_EXEC package $EXTRA_ENV -DskipTests=true -Dbecpg.dockerbuild.name="test"
   	 docker compose -f ./becpg-integration-runner/target/docker-compose-build.yml build
   	 COMPOSE_FILE="./becpg-integration-runner/target/docker-compose-build.yml"

     docker compose -f $COMPOSE_FILE build becpg-base-core
     docker compose -f $COMPOSE_FILE build becpg-base-share
     docker compose -f $COMPOSE_FILE build becpg-test-core
     docker compose -f $COMPOSE_FILE build becpg-test-share
     
   fi 

}

install() {
  if [ -d becpg-enterprise ]; then
    cd becpg-enterprise
    $MVN_EXEC  clean install $EXTRA_ENV -DskipTests=true
    COMPOSE_FILE="./distribution/target/docker-compose-build.yml"
    
    docker compose -f $COMPOSE_FILE build becpg-db becpg-solr becpg-mail becpg-ids becpg-http
    cd ..
   else
    $MVN_EXEC  clean install $EXTRA_ENV -DskipTests=true
    COMPOSE_FILE="./becpg-integration-runner/target/docker-compose-build.yml"
    
    docker compose -f $COMPOSE_FILE build becpg-db becpg-solr
  fi
  
}

install_hotswap(){
	mkdir -p /opt/hotswap/jvm
	curl -sL  https://cache-redirector.jetbrains.com/intellij-jbr/jbr_jcef-21.0.4-linux-x64-b607.1.tar.gz \
	      -o /opt/hotswap/jbr_jcef-21.0.4-linux-x64-b607.1.tar.gz && \
	     mkdir -p /usr/java && tar -xvf /opt/hotswap/jbr_jcef-21.0.4-linux-x64-b607.1.tar.gz -C /opt/hotswap/jvm && \
	     rm /opt/hotswap/jbr_jcef-21.0.4-linux-x64-b607.1.tar.gz 
	    
	mkdir -p /opt/hotswap/jvm/jbr_jcef-21.0.4-linux-x64-b607.1/lib/hotswap/ && \
	     curl -sL https://github.com/HotswapProjects/HotswapAgent/releases/download/RELEASE-2.0.1/hotswap-agent-2.0.1.jar -o  \
	     /opt/hotswap/jvm/jbr_jcef-21.0.4-linux-x64-b607.1/lib/hotswap/hotswap-agent.jar
    ln -sfn /opt/hotswap/jvm/jbr_jcef-21.0.4-linux-x64-b607.1 /opt/hotswap/jvm/latest
	     
	echo -e "Append to docker-compose.override.yml : -XX:+AllowEnhancedClassRedefinition -XX:HotswapAgent=fatjar\n\
	volumes:\n\
	  - becpg_data:/usr/local/tomcat/data\n\
	  - ../../becpg-core/target/classes:/usr/local/tomcat/hotswap-agent/becpg-core/target/classes\n\
	  - ../../becpg-plm/becpg-plm-core/target/classes:/usr/local/tomcat/hotswap-agent/becpg-plm-core/target/classes\n\
	  - ../../becpg-project/becpg-project-core/target/classes:/usr/local/tomcat/hotswap-agent/becpg-project-core/target/classes\n\
	  - ../../becpg-integration-runner/target/test-classes:/usr/local/tomcat/hotswap-agent/becpg-integration-runner/target/test-classes\n\
	  - /opt/hotswap/jvm/latest:/etc/alternatives/jre"
}

tail() {
    docker compose -p $BECPG_VERSION_PROFILE -f $COMPOSE_FILE_PATH -f docker-compose.override.yml logs -f --tail=100 becpg-share becpg
}

test() {
    $MVN_EXEC verify $EXTRA_ENV
}

reindex() {
    docker compose -p $BECPG_VERSION_PROFILE -f $COMPOSE_FILE_PATH -f docker-compose.override.yml stop solr
    docker compose  -p $BECPG_VERSION_PROFILE -f $COMPOSE_FILE_PATH -f docker-compose.override.yml rm -v solr
    docker volume rm ${BECPG_VERSION_PROFILE}_solr_data
	docker compose -p $BECPG_VERSION_PROFILE -f $COMPOSE_FILE_PATH -f docker-compose.override.yml up -d solr
}


case "$1" in
  install)
    install
    ;;
  install_hotswap)
    install_hotswap
    ;;  
  build_start)
    build
    start
    tail
    ;;
  build_test)
    build
    start_test
    test
    tail
    down_test
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
