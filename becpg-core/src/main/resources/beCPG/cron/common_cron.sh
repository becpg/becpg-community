#!/bin/sh

################
# Environnement
################

export ALF_DATA=${alfresco.data.location} 

#####################################
# Paramètres de configuration SQL
#####################################

# Mysql
MYSQL_HOST=${alfresco.db.host}
MYSQL_SCHEMA=${alfresco.db.name}
MYSQL_USER=${alfresco.db.username}
MYSQL_PWD=${alfresco.db.password}



