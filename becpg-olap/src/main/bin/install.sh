#!/bin/bash
. ./common.sh

echo "**********************************************************"
echo "Copy files to /etc/becpg/olap"
echo "**********************************************************"

mkdir -p /etc/becpg/olap/sql
cp common.sh /etc/becpg/olap
cp sql/* /etc/becpg/olap/sql

echo "**********************************************************"
echo "Add view and procs to BD"
echo "**********************************************************"
 cd sql/
 ./setup_db.sh
 cd ..

#Call deploy proc
./deploy.sh

echo "**********************************************************"
echo " Don't forget to add crontab : (crontab -e)"
echo "# m h  dom mon dow   command"
echo "# 3AM Every Day "
echo "0 3 * * *	/etc/becpg/olap/sql/cron.sh"
echo "**********************************************************"