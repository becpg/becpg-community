#!/bin/bash
. ../common.sh

echo "Run  mysql-view.sql"
time mysql --user=$MYSQL_USER --password=$MYSQL_PWD $MYSQL_SCHEMA < mysql-view.sql
echo "Run  mysql-proc.sql"
time mysql --user=$MYSQL_USER --password=$MYSQL_PWD $MYSQL_SCHEMA < mysql-proc.sql
echo "Run  mysql-cron.sql"
time mysql --user=$MYSQL_USER --password=$MYSQL_PWD $MYSQL_SCHEMA < mysql-cron.sql
