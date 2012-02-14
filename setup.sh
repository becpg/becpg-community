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
cd $BECPG_OLAP_ROOT
mvn clean package -Dmaven.test.skip=true  $MVN_PROFILE
cd target 
tar xvfz becpg-olap-*-distribution.tar.gz
cd becpg-olap-*
echo `pwd`
./install.sh
cd $BECPG_OLAP_ROOT/target
rm -rf becpg-olap-*


