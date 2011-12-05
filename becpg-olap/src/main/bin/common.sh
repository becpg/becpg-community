#!/bin/sh

################
# Environnement
################

export SERVER=${olap.server}/tomcat

#####################################
# Paramètres de configuration OLAP
#####################################

# Mysql
MYSQL_HOST=${olap.db.host}
MYSQL_PORT=${olap.db.port}
MYSQL_SCHEMA=${olap.db.name}
MYSQL_USER=${olap.db.username}
MYSQL_PWD=${olap.db.password}

