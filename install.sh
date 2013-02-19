#!/bin/bash
. ./common.sh


if [ $# -ne 1 ]
   then
      echo "Usage: $0 <instance name>"
      exit 0
fi

export SERVER=$INSTANCE_DIR/$1


mkdir -p $BECPG_ROOT/logs
export FICHIER_LOG=$BECPG_ROOT/logs/install.log
rm -f $FICHIER_LOG

# ==============================================================================
# Initialisation des fonctions
# ==============================================================================

# Fonction d'affichage et de redirection des sorties STD et ERR
fct_echo() {
    echo "$*"
    echo "$*" >>$FICHIER_LOG 2>&1
}

# Fonction de test de la présence du répertoire d'installation
fct_exist() {
    if [ -e $1 ] ;
    then fct_echo "# [OK] Répertoire $1 exist";
    else fct_echo "# [KO] Répertoire $1 doesn't exist";exit 1;
    fi
}

# Fonction de test de la présence du répertoire d'installation
fct_exist_file() {
    if [ -f $1 ] ;
    then fct_echo "# [OK] Fichier $1 exist";
    else fct_echo "# [KO] Fichier $1 doesn't exist";exit 1;
    fi
}

# Fonction de creation du repertoire d'installation
fct_create () {
    if [ -e $1 ] ;
    then fct_echo "# [OK] Répertoire $1 exist";
    else
        fct_echo "# [OK] Répertoire $1 doesn't exist : create folder"
        mkdir $1 >>$FICHIER_LOG 2>1
        fct_test "mkdir $1"
        fct_exist "$1"
    fi
}

# Fonction de test des valeurs de retour des commandes
fct_test() {
    if [ $? == 0 ];
    then fct_echo "# [OK] $1";
    else fct_echo "# [KO] $1";exit 1;
    fi
}

# Fonction maven install
# $1 : classifier
# $2 : groupId
# $3 : artifactId
# $4 : version
# $5 : file 

fct_mvn_install(){
	mvn install:install-file -Dclassifier=$1  -DgroupId=$2 -DartifactId=$3 -Dversion=$4 -Dpackaging=jar -Dfile=$5  >>$FICHIER_LOG 2>1 &
	spanner "$!" '/\-'
}

## Adjust to taste (or leave empty)
SP_COLOUR="\e[32;40m"
SP_WIDTH=1.1  ## Try: SP_WIDTH=5.5
SP_DELAY=.2

# Spinner
spanner(){
	 SP_STRING=${2:-"'|/=\'"}
    while [ -d /proc/$1 ]
    do
        printf "$SP_COLOUR\e7  %${SP_WIDTH}s  \e8\e[0m" "$SP_STRING"
        sleep ${SP_DELAY:-.2}
        SP_STRING=${SP_STRING#"${SP_STRING%?}"}${SP_STRING%?}
    done
}


# ==============================================================================
# Entête du fichier de log
# ==============================================================================
fct_echo "# ==================================================================="
fct_echo "# Project : beCPG."
fct_echo "# Product of beCPG"
fct_echo "# Copyright 2010-2012 beCPG."
fct_echo "#"
fct_echo "# Installing development environment."
fct_echo "# ==================================================================="
date >>$FICHIER_LOG 2>&1


read -p  "Do you want to install addons libs ? (y/n)" ans
if [ "$ans" = "y" ]; then

cd $BOOTSTRAP_HOME/addons
echo -ne "Installing ... "
fct_mvn_install repo de.fme jsconsole 0.5.1 javascript-console-repo-0.5.1.jar
fct_mvn_install share de.fme jsconsole 0.5.1 javascript-console-share-0.5.1.jar
fi

#read -p "Do you want to install locally Alfresco Patch ? (y/n)" ans 
#if [ "$ans" = "y" ]; then
#fct_echo "Installing alfresco Patch"
#cd $BECPG_ROOT/alfresco-patch
#echo -ne "Installing ... "
#mvn clean install -Dmaven.test.skip=true >>$FICHIER_LOG 2>1 &
#spanner "$!" '/\-'
#echo ""
#fi

#read -p "Do you want to deploy to remote repository Alfresco Patch ? (y/n)" ans 
#if [ "$ans" = "y" ]; then
#fct_echo "Deploying alfresco Patch"
#cd $BECPG_ROOT/alfresco-patch
#echo -ne "Deploying ... "
#mvn clean deploy -Dmaven.test.skip=true >>$FICHIER_LOG 2>1 &
#spanner "$!" '/\-'
#echo ""
#fi

read -p "Launch mysql tunning? (y/n)" ans 
if [ "$ans" = "y" ]; then
 cd $BECPG_ROOT/tools
 ./mysqltuner.pl
fi

