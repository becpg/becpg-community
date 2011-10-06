#!/bin/bash
. ./common.sh

fct_copy_old () {
    if [ -e $1 ] ;
    then 
        echo "# [OK] file $1 is present"
        cp $1 $1.old
        echo "# [OK] Copy file $1 with success";
    else echo "# No file $1 to save";
    fi
}


 echo "**********************************************************"
 echo "Add view and procs to BD"
 echo "**********************************************************"
 cd $BECPG_OLAP_ROOT/sql/
 ./setup_db.sh
 cd $BECPG_OLAP_ROOT



echo "**********************************************************"
echo "Configure OLAP DataSource"
echo "**********************************************************"
fct_copy_old "$BECPG_OLAP_ROOT/conf/becpg"
cp $BECPG_OLAP_ROOT/conf/becpg.sample $BECPG_OLAP_ROOT/conf/becpg
sed -i 's/\$MYSQL_HOST\$/'$MYSQL_HOST'/g' $BECPG_OLAP_ROOT/conf/becpg
sed -i 's/\$MYSQL_PORT\$/'$MYSQL_PORT'/g' $BECPG_OLAP_ROOT/conf/becpg
sed -i 's/\$MYSQL_SCHEMA\$/'$MYSQL_SCHEMA'/g' $BECPG_OLAP_ROOT/conf/becpg
sed -i 's/\$MYSQL_USER\$/'$MYSQL_USER'/g' $BECPG_OLAP_ROOT/conf/becpg
sed -i 's/\$MYSQL_PWD\$/'$MYSQL_PWD'/g' $BECPG_OLAP_ROOT/conf/becpg

echo "**********************************************************"
echo "Configure sample crontab"
echo "**********************************************************"
fct_copy_old "$BECPG_OLAP_ROOT/sql/crontab"
cp $BECPG_OLAP_ROOT/sql/crontab.sample $BECPG_OLAP_ROOT/sql/crontab
sed -i 's/\$MYSQL_HOST\$/'$MYSQL_HOST'/g' $BECPG_OLAP_ROOT/sql/crontab
sed -i 's/\$MYSQL_PORT\$/'$MYSQL_PORT'/g' $BECPG_OLAP_ROOT/sql/crontab
sed -i 's/\$MYSQL_SCHEMA\$/'$MYSQL_SCHEMA'/g' $BECPG_OLAP_ROOT/sql/crontab
sed -i 's/\$MYSQL_USER\$/'$MYSQL_USER'/g' $BECPG_OLAP_ROOT/sql/crontab
sed -i 's/\$MYSQL_PWD\$/'$MYSQL_PWD'/g' $BECPG_OLAP_ROOT/sql/crontab