#!/bin/bash
. ../../common.sh

echo "Run  mysql-view.sql"
mysql --user=$MYSQL_USER --password=$MYSQL_PWD $MYSQL_SCHEMA < mysql-view.sql
echo "Run  mysql-proc.sql"
mysql --user=$MYSQL_USER --password=$MYSQL_PWD $MYSQL_SCHEMA < mysql-proc.sql
echo "Run  mysql-cron.sql"
mysql --user=$MYSQL_USER --password=$MYSQL_PWD $MYSQL_SCHEMA < mysql-cron.sql