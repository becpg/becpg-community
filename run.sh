#!/bin/sh

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

purge() {
    docker-compose -f  $COMPOSE_FILE_PATH down -v
}

build() {
    $MVN_EXEC clean package -DskipTests=true
}



tail() {
    docker-compose -f $COMPOSE_FILE_PATH logs -f
}

tail_all() {
    docker-compose -f $COMPOSE_FILE_PATH logs --tail="50"
}

prepare_test() {
    $MVN_EXEC verify -DskipTests=true -pl becpg-integration-runner
}

test() {
    $MVN_EXEC verify -pl becpg-integration-runner
}

case "$1" in
  build_start)
    down
    build
    start
    tail
    ;;
  build_start_it_supported)
    down
    build
    prepare_test
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
    down
    build
   # prepare_test
    start
    test
    tail_all
    down
    ;;
  test)
    test
    ;;
  *)
    echo "Usage: $0 {build_start|build_start_it_supported|start|stop|purge|tail|build_test|test}"
esac