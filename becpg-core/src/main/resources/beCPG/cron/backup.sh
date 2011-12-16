#
#	cron executed at 3:30am after Alfresco index backup job done at 3am
#
# Don't forget to add crontab : (crontab -e)
# m h  dom mon dow   command
# 3:30AM Every Day
# 30 3 * * *	/opt/alfresco-3.4.d/tomcat/webapps/alfresco/WEB-INF/classes/beCPG/cron/backup.sh


#!/bin/bash
. common_cron.sh

echo "Run beCPG backup cron"
export BACKUP=$ALF_DATA/../backup
cd $ALF_DATA

if [ ! -d "$BACKUP" ]
then
	echo "create $BACKUP directory"	
	mkdir $BACKUP
fi

echo "backup SQL"
if [ -f "$BACKUP/beCPG-backup.sql" ]
then
	echo "remove $BACKUP/beCPG-backup.sql file"	
	rm $BACKUP/beCPG-backup.sql
fi
mysqldump --user=$MYSQL_USER --password=$MYSQL_PWD $MYSQL_SCHEMA > $BACKUP/beCPG-backup.sql

echo "backup files"
rsync -a --delete-after $ALF_DATA/contentstore $BACKUP/contentstore
rsync -a --delete-after $ALF_DATA/contentstore.deleted $BACKUP/contentstore.deleted
if [ -d "$ALF_DATA/audit.contentstore" ]
then
	rsync -a --delete-after $ALF_DATA/audit.contentstore $BACKUP/audit.contentstore
fi
rsync -a --delete-after $ALF_DATA/backup-lucene-indexes $BACKUP/backup-lucene-indexes
