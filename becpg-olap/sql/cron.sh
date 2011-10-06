#!/bin/bash
. ../../common.sh

echo "Run  mysql-cron.sql"
time mysql --user=$MYSQL_USER --password=$MYSQL_PWD $MYSQL_SCHEMA < mysql-cron.sql