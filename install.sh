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

fct_mvn_install(){
	mvn install:install-file -Dclassifier=$1  -DgroupId=$2 -DartifactId=$3 -Dversion=$4 -Dpackaging=jar -Dfile=$3.jar  >>$FICHIER_LOG 2>1 &
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

read -p "Do you want to install Alfresco libs ? (y/n)" ans
if [ "$ans" = "y" ]; then

cd $SERVER/webapps/alfresco/WEB-INF/lib/
echo -ne "Installing ... "
fct_mvn_install "community" "org.alfresco.sdk" "abdera-client-0.4.0-incubating" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "abdera-core-0.4.0-incubating" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "abdera-extensions-json-0.4.0-incubating" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "abdera-i18n-0.4.0-incubating" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "abdera-parser-0.4.0-incubating" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "acegi-security-0.8.2_patched" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "activation-1.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "activiti-engine-5.7" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "activiti-spring-5.7" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "addressing-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "alfresco-core-4.0.d" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "alfresco-data-model-4.0.d" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "alfresco-deployment-4.0.d" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "alfresco-jlan-embed-4.0.d" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "alfresco-mbeans-4.0.d" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "alfresco-opencmis-extension-0.2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "alfresco-remote-api-4.0.d" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "alfresco-repository-4.0.d" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "alfresco-wdr-deployment" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "alfresco-web-client-4.0.d" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "alfresco-web-framework-commons-4.0.d" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "ant-1.7.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "antlr-3.3-complete" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "aopalliance" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "apache-mime4j-0.6" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "apache-solr-solrj-1.4.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "asm-3.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "avalon-framework-4.2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "axiom-api-1.2.5" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "axiom-impl-1.2.5" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "axis-1.4" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "batik-all-1.6" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "bcel" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "bcmail-jdk15-1.45" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "bcprov-jdk15-1.45" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "bliki-3.0.2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "boilerpipe-1.1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "bsf-2.4.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "bsh-1.3.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "cglib-2.2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "chemistry-abdera-0.1-incubating-SNAPSHOT" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "chemistry-opencmis-client-api-0.6.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "chemistry-opencmis-client-bindings-0.6.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "chemistry-opencmis-client-impl-0.6.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "chemistry-opencmis-commons-api-0.6.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "chemistry-opencmis-commons-impl-0.6.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "chemistry-opencmis-server-bindings-0.6.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "chemistry-opencmis-server-support-0.6.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "chemistry-opencmis-test-browser-0.6.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "chemistry-opencmis-test-tck-0.6.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "chemistry-tck-atompub-0.1-incubating-SNAPSHOT" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "chiba-1.3.0-patched" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-beanutils-1.7.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-codec-1.4" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-collections-3.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-compress-1.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-csv-20110211" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-dbcp-1.4-patched" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-digester-1.6" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-discovery-0.2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-el" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-fileupload-1.2.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-httpclient-3.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-io-1.4" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-jxpath-1.2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-lang-2.6" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-logging-1.1.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-modeler" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-net-2.2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-pool-1.5.5" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "commons-validator-1.4-SNAPSHOT-20110316" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "cxf-2.2.2-patched" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "dom4j-1.6.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "drew-image-metadata-extractor-2.4.0-beta-1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "ehcache-core-2.0.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "facebook_070716" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "FastInfoset-1.2.2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "fontbox-1.6.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "fop-0.94" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "freemarker-2.3.18-patched" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-analytics-2.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-analytics-meta-2.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-appsforyourdomain-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-appsforyourdomain-meta-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-base-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-blogger-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-blogger-meta-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-books-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-books-meta-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-calendar-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-calendar-meta-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-client-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-client-meta-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-codesearch-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-codesearch-meta-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-contacts-3.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-contacts-meta-3.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-core-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-docs-3.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-docs-meta-3.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-finance-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-finance-meta-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-gtt-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-gtt-meta-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-health-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-health-meta-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-maps-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-maps-meta-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-media-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-photos-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-photos-meta-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-projecthosting-2.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-projecthosting-meta-2.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-sidewiki-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-sidewiki-meta-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-sites-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-sites-meta-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-spreadsheet-3.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-spreadsheet-meta-3.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-webmastertools-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-webmastertools-meta-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-youtube-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "gdata-youtube-meta-2.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "geronimo-activation_1.1_spec-1.0.2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "geronimo-annotation_1.0_spec-1.1.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "geronimo-jaxws_2.1_spec-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "geronimo-stax-api_1.0_spec-1.0.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "geronimo-ws-metadata_2.0_spec-1.1.2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "google-collect-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "greenmail-1.3-patched" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "groovy-1.7.5" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "guessencoding-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "hazelcast-1.9.4.4" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "hibernate-3.2.6-patched" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "hrtlib" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "htmlparser-1.6" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "httpclient-4.1.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "httpclient-cache-4.1.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "httpcore-4.1.3" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "icu4j_3_6_1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jackson-core-asl-1.8.3" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jackson-mapper-asl-1.8.3" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jakarta-oro-2.0.8" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jaxb-api-2.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jaxb-impl-2.1.11" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jaxb-xjc-2.1.7" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jaxen-1.1-beta-8" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jaxrpc" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jaxws-api-2.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jaxws-rt-2.1.7" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jbpm-identity-3.3.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jbpm-jpdl-3.3.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jcr-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jempbox-1.6.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jgroups-2.11.1.Final" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jibx-bind" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jibx-run" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jid3lib-0.5.4" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "JMagick" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "joda-time-1.2.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jooconverter-2.1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "JSlideShare-0.6" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "json-simple-1.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "json" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jsr107cache-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jsr181-api-1.0-MR1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jsr250-api-1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jstl-1.1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jta" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jtds-1.2.5" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jug-asl-2.0.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "junit-4.8.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jut" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "jutf7-1.0.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "livetribe-jsr223-2.0.6" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "log4j-1.2.15" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "lucene-analyzers-2.4.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "lucene-core-2.4.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "lucene-regex-2.4.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "lucene-snowball-2.4.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "mail" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "mimepull-1.3" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "mockito-all-1.8.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "mybatis-3.0.4-patched" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "mybatis-spring-1.0.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "myfaces-api-1.1.8" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "myfaces-impl-1.1.8" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "neethi-2.0.4" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "netcdf-4.2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "odmg-3.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "ooxml-schemas-1.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "openoffice-juh-3.1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "openoffice-jurt-3.1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "openoffice-ridl-3.1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "openoffice-sandbox-2.0.3" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "openoffice-unoil-3.1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "opensaml-1.0.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "org.springframework.aop-3.0.5.RELEASE" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "org.springframework.asm-3.0.5.RELEASE" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "org.springframework.beans-3.0.5.RELEASE" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "org.springframework.context-3.0.5.RELEASE" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "org.springframework.context.support-3.0.5.RELEASE" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "org.springframework.core-3.0.5.RELEASE" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "org.springframework.expression-3.0.5.RELEASE" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "org.springframework.jdbc-3.0.5.RELEASE" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "org.springframework.orm-3.0.5.RELEASE" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "org.springframework.transaction-3.0.5.RELEASE" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "org.springframework.web-3.0.5.RELEASE" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "org.springframework.web.servlet-3.0.5.RELEASE" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "pdfbox-1.6.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "PDFRenderer-0.9.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "poi-3.8-beta5-20111128" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "poi-ooxml-3.8-beta5-20111128" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "poi-scratchpad-3.8-beta5-20111128" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "quartz-1.8.3-patched" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "resolver-20050927" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "rhino-js-1.6R7" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "rome-0.9" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "saxpath" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "shale-test-1.0.4" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "slf4j-api-1.5.11" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "slf4j-log4j12-1.5.11" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "spring-cmis-framework-1.0.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "spring-security-core-3.1.0.RC2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "spring-social-core-1.0.0.RC1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "spring-social-facebook-1.0.0.RC1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "spring-social-facebook-web-1.0.0.RC1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "spring-social-linkedin-1.0.0.BUILD-SNAPSHOT" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "spring-social-test-1.0.0.RC1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "spring-social-twitter-1.0.0.RC1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "spring-social-web-1.0.0.RC1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "spring-surf-1.0.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "spring-surf-api-1.0.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "spring-surf-core-1.0.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "spring-surf-core-configservice-1.0.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "spring-webscripts-1.0.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "spring-webscripts-api-1.0.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "springmodules-jbpm31" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "standard" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "stax-api-1.0.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "stax-ex-1.2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "stax-utils-20060502" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "streambuffer-0.9" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "subethasmtp-3.1.6" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "tagsoup-1.2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "tika-core-1.1-20111128" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "tika-parsers-1.1-20111128" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "truezip" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "urlrewritefilter-3.1.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "vorbis-java-core-0.1-SNAPSHOT" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "vorbis-java-tika-0.1-SNAPSHOT" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "wsdl4j-1.6.2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "wss4j-1.5.4-patched" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "wstx-asl-3.2.4" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "xercesImpl-2.8.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "xml-resolver-1.2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "xmlbeans-2.3.0" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "xmlgraphics-commons-1.2" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "xmlrpc" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "XmlSchema-1.4.5" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "xmlsec-1.4.1" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "xpp3-1.1.3_8" "4.0.d"
fct_mvn_install "community" "org.alfresco.sdk" "xstream-1.2.2" "4.0.d"

fct_echo  "Installing Additional libs"

cd $BOOTSTRAP_HOME/alfresco-sdk/lib
echo -ne "Installing ... "
fct_mvn_install "community" "org.alfresco.sdk" "config" "4.0.d"
cd ..
mvn install >>$FICHIER_LOG 2>&1 &
spanner "$!" '/\-'
echo ""
fi

read -p  "Do you want to install Birt libs ? (y/n)" ans
if [ "$ans" = "y" ]; then

cd $BOOTSTRAP_HOME/birt-sdk/lib
echo -ne "Installing ... "
sh mavenize.sh  >>$FICHIER_LOG 2>&1 &
spanner "$!" '/\-'
cd ..
mvn install >>$FICHIER_LOG 2>&1 &
spanner "$!" '/\-'
echo ""
fi

read -p "Do you want to install locally Alfresco Patch ? (y/n)" ans 
if [ "$ans" = "y" ]; then
fct_echo "Installing alfresco Patch"
cd $BECPG_ROOT/alfresco-patch
echo -ne "Installing ... "
mvn clean install -Dmaven.test.skip=true >>$FICHIER_LOG 2>1 &
spanner "$!" '/\-'
echo ""
fi

read -p "Do you want to deploy to remote repository Alfresco Patch ? (y/n)" ans 
if [ "$ans" = "y" ]; then
fct_echo "Deploying alfresco Patch"
cd $BECPG_ROOT/alfresco-patch
echo -ne "Deploying ... "
mvn clean deploy -Dmaven.test.skip=true >>$FICHIER_LOG 2>1 &
spanner "$!" '/\-'
echo ""
fi

read -p "Launch mysql tunning? (y/n)" ans 
if [ "$ans" = "y" ]; then
 cd $BECPG_ROOT/tools
 ./mysqltuner.pl
fi

