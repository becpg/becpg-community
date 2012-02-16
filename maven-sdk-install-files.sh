#!/bin/sh
. ./common.sh

echo "Install alfresco libs"


#cd $ALF_SDK/lib/server/

#mvn install:install-file -Dfile=dependencies/spring-surf/spring-surf-1.0.0.CI-SNAPSHOT.jar -DgroupId=org.alfresco.sdk -DartifactId=spring-surf-1.0.0.CI-SNAPSHOT -Dversion=3.4 -Dpackaging=jar -DgeneratePom=true
#mvn install:install-file -Dfile=dependencies/spring-surf/spring-webscripts-1.0.0.CI-SNAPSHOT-tests.jar -DgroupId=org.alfresco.sdk -DartifactId=spring-webscripts-1.0.0.CI-SNAPSHOT-tests -Dversion=3.4 -Dpackaging=jar  -DgeneratePom=true
#mvn install:install-file -Dfile=dependencies/spring-surf/spring-webscripts-1.0.0.CI-SNAPSHOT.jar -DgroupId=org.alfresco.sdk -DartifactId=spring-webscripts-1.0.0.CI-SNAPSHOT -Dversion=3.4 -Dpackaging=jar -DgeneratePom=true
#mvn install:install-file -Dfile=dependencies/spring-surf/spring-webscripts-api-1.0.0.CI-SNAPSHOT.jar -DgroupId=org.alfresco.sdk -DartifactId=spring-webscripts-api-1.0.0.CI-SNAPSHOT -Dversion=3.4 -Dpackaging=jar -DgeneratePom=true


cd $SERVER/webapps/alfresco/WEB-INF/lib/


mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=abdera-client-0.4.0-incubating -Dversion=4.0.d -Dpackaging=jar -Dfile=abdera-client-0.4.0-incubating.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=abdera-core-0.4.0-incubating -Dversion=4.0.d -Dpackaging=jar -Dfile=abdera-core-0.4.0-incubating.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=abdera-extensions-json-0.4.0-incubating -Dversion=4.0.d -Dpackaging=jar -Dfile=abdera-extensions-json-0.4.0-incubating.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=abdera-i18n-0.4.0-incubating -Dversion=4.0.d -Dpackaging=jar -Dfile=abdera-i18n-0.4.0-incubating.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=abdera-parser-0.4.0-incubating -Dversion=4.0.d -Dpackaging=jar -Dfile=abdera-parser-0.4.0-incubating.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=acegi-security-0.8.2_patched -Dversion=4.0.d -Dpackaging=jar -Dfile=acegi-security-0.8.2_patched.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=activation-1.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=activation-1.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=activiti-engine-5.7 -Dversion=4.0.d -Dpackaging=jar -Dfile=activiti-engine-5.7.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=activiti-spring-5.7 -Dversion=4.0.d -Dpackaging=jar -Dfile=activiti-spring-5.7.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=addressing-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=addressing-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=alfresco-core-4.0.d -Dversion=4.0.d -Dpackaging=jar -Dfile=alfresco-core-4.0.d.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=alfresco-data-model-4.0.d -Dversion=4.0.d -Dpackaging=jar -Dfile=alfresco-data-model-4.0.d.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=alfresco-deployment-4.0.d -Dversion=4.0.d -Dpackaging=jar -Dfile=alfresco-deployment-4.0.d.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=alfresco-jlan-embed-4.0.d -Dversion=4.0.d -Dpackaging=jar -Dfile=alfresco-jlan-embed-4.0.d.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=alfresco-mbeans-4.0.d -Dversion=4.0.d -Dpackaging=jar -Dfile=alfresco-mbeans-4.0.d.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=alfresco-opencmis-extension-0.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=alfresco-opencmis-extension-0.2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=alfresco-remote-api-4.0.d -Dversion=4.0.d -Dpackaging=jar -Dfile=alfresco-remote-api-4.0.d.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=alfresco-repository-4.0.d -Dversion=4.0.d -Dpackaging=jar -Dfile=alfresco-repository-4.0.d.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=alfresco-wdr-deployment -Dversion=4.0.d -Dpackaging=jar -Dfile=alfresco-wdr-deployment.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=alfresco-web-client-4.0.d -Dversion=4.0.d -Dpackaging=jar -Dfile=alfresco-web-client-4.0.d.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=alfresco-web-framework-commons-4.0.d -Dversion=4.0.d -Dpackaging=jar -Dfile=alfresco-web-framework-commons-4.0.d.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=ant-1.7.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=ant-1.7.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=antlr-3.3-complete -Dversion=4.0.d -Dpackaging=jar -Dfile=antlr-3.3-complete.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=aopalliance -Dversion=4.0.d -Dpackaging=jar -Dfile=aopalliance.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=apache-mime4j-0.6 -Dversion=4.0.d -Dpackaging=jar -Dfile=apache-mime4j-0.6.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=apache-solr-solrj-1.4.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=apache-solr-solrj-1.4.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=asm-3.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=asm-3.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=avalon-framework-4.2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=avalon-framework-4.2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=axiom-api-1.2.5 -Dversion=4.0.d -Dpackaging=jar -Dfile=axiom-api-1.2.5.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=axiom-impl-1.2.5 -Dversion=4.0.d -Dpackaging=jar -Dfile=axiom-impl-1.2.5.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=axis-1.4 -Dversion=4.0.d -Dpackaging=jar -Dfile=axis-1.4.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=batik-all-1.6 -Dversion=4.0.d -Dpackaging=jar -Dfile=batik-all-1.6.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=bcel -Dversion=4.0.d -Dpackaging=jar -Dfile=bcel.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=bcmail-jdk15-1.45 -Dversion=4.0.d -Dpackaging=jar -Dfile=bcmail-jdk15-1.45.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=bcprov-jdk15-1.45 -Dversion=4.0.d -Dpackaging=jar -Dfile=bcprov-jdk15-1.45.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=bliki-3.0.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=bliki-3.0.2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=boilerpipe-1.1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=boilerpipe-1.1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=bsf-2.4.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=bsf-2.4.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=bsh-1.3.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=bsh-1.3.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=cglib-2.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=cglib-2.2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=chemistry-abdera-0.1-incubating-SNAPSHOT -Dversion=4.0.d -Dpackaging=jar -Dfile=chemistry-abdera-0.1-incubating-SNAPSHOT.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=chemistry-opencmis-client-api-0.6.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=chemistry-opencmis-client-api-0.6.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=chemistry-opencmis-client-bindings-0.6.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=chemistry-opencmis-client-bindings-0.6.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=chemistry-opencmis-client-impl-0.6.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=chemistry-opencmis-client-impl-0.6.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=chemistry-opencmis-commons-api-0.6.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=chemistry-opencmis-commons-api-0.6.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=chemistry-opencmis-commons-impl-0.6.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=chemistry-opencmis-commons-impl-0.6.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=chemistry-opencmis-server-bindings-0.6.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=chemistry-opencmis-server-bindings-0.6.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=chemistry-opencmis-server-support-0.6.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=chemistry-opencmis-server-support-0.6.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=chemistry-opencmis-test-browser-0.6.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=chemistry-opencmis-test-browser-0.6.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=chemistry-opencmis-test-tck-0.6.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=chemistry-opencmis-test-tck-0.6.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=chemistry-tck-atompub-0.1-incubating-SNAPSHOT -Dversion=4.0.d -Dpackaging=jar -Dfile=chemistry-tck-atompub-0.1-incubating-SNAPSHOT.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=chiba-1.3.0-patched -Dversion=4.0.d -Dpackaging=jar -Dfile=chiba-1.3.0-patched.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-beanutils-1.7.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-beanutils-1.7.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-codec-1.4 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-codec-1.4.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-collections-3.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-collections-3.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-compress-1.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-compress-1.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-csv-20110211 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-csv-20110211.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-dbcp-1.4-patched -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-dbcp-1.4-patched.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-digester-1.6 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-digester-1.6.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-discovery-0.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-discovery-0.2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-el -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-el.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-fileupload-1.2.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-fileupload-1.2.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-httpclient-3.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-httpclient-3.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-io-1.4 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-io-1.4.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-jxpath-1.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-jxpath-1.2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-lang-2.6 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-lang-2.6.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-logging-1.1.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-logging-1.1.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-modeler -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-modeler.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-net-2.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-net-2.2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-pool-1.5.5 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-pool-1.5.5.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=commons-validator-1.4-SNAPSHOT-20110316 -Dversion=4.0.d -Dpackaging=jar -Dfile=commons-validator-1.4-SNAPSHOT-20110316.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=cxf-2.2.2-patched -Dversion=4.0.d -Dpackaging=jar -Dfile=cxf-2.2.2-patched.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=dom4j-1.6.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=dom4j-1.6.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=drew-image-metadata-extractor-2.4.0-beta-1 -Dversion=4.0.d -Dpackaging=jar -Dfile=drew-image-metadata-extractor-2.4.0-beta-1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=ehcache-core-2.0.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=ehcache-core-2.0.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=facebook_070716 -Dversion=4.0.d -Dpackaging=jar -Dfile=facebook_070716.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=FastInfoset-1.2.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=FastInfoset-1.2.2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=fontbox-1.6.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=fontbox-1.6.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=fop-0.94 -Dversion=4.0.d -Dpackaging=jar -Dfile=fop-0.94.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=freemarker-2.3.18-patched -Dversion=4.0.d -Dpackaging=jar -Dfile=freemarker-2.3.18-patched.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-analytics-2.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-analytics-2.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-analytics-meta-2.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-analytics-meta-2.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-appsforyourdomain-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-appsforyourdomain-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-appsforyourdomain-meta-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-appsforyourdomain-meta-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-base-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-base-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-blogger-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-blogger-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-blogger-meta-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-blogger-meta-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-books-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-books-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-books-meta-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-books-meta-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-calendar-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-calendar-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-calendar-meta-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-calendar-meta-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-client-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-client-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-client-meta-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-client-meta-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-codesearch-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-codesearch-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-codesearch-meta-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-codesearch-meta-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-contacts-3.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-contacts-3.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-contacts-meta-3.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-contacts-meta-3.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-core-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-core-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-docs-3.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-docs-3.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-docs-meta-3.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-docs-meta-3.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-finance-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-finance-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-finance-meta-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-finance-meta-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-gtt-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-gtt-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-gtt-meta-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-gtt-meta-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-health-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-health-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-health-meta-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-health-meta-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-maps-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-maps-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-maps-meta-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-maps-meta-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-media-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-media-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-photos-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-photos-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-photos-meta-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-photos-meta-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-projecthosting-2.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-projecthosting-2.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-projecthosting-meta-2.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-projecthosting-meta-2.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-sidewiki-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-sidewiki-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-sidewiki-meta-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-sidewiki-meta-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-sites-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-sites-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-sites-meta-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-sites-meta-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-spreadsheet-3.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-spreadsheet-3.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-spreadsheet-meta-3.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-spreadsheet-meta-3.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-webmastertools-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-webmastertools-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-webmastertools-meta-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-webmastertools-meta-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-youtube-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-youtube-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=gdata-youtube-meta-2.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=gdata-youtube-meta-2.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=geronimo-activation_1.1_spec-1.0.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=geronimo-activation_1.1_spec-1.0.2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=geronimo-annotation_1.0_spec-1.1.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=geronimo-annotation_1.0_spec-1.1.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=geronimo-jaxws_2.1_spec-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=geronimo-jaxws_2.1_spec-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=geronimo-stax-api_1.0_spec-1.0.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=geronimo-stax-api_1.0_spec-1.0.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=geronimo-ws-metadata_2.0_spec-1.1.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=geronimo-ws-metadata_2.0_spec-1.1.2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=google-collect-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=google-collect-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=greenmail-1.3-patched -Dversion=4.0.d -Dpackaging=jar -Dfile=greenmail-1.3-patched.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=groovy-1.7.5 -Dversion=4.0.d -Dpackaging=jar -Dfile=groovy-1.7.5.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=guessencoding-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=guessencoding-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=hazelcast-1.9.4.4 -Dversion=4.0.d -Dpackaging=jar -Dfile=hazelcast-1.9.4.4.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=hibernate-3.2.6-patched -Dversion=4.0.d -Dpackaging=jar -Dfile=hibernate-3.2.6-patched.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=hrtlib -Dversion=4.0.d -Dpackaging=jar -Dfile=hrtlib.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=htmlparser-1.6 -Dversion=4.0.d -Dpackaging=jar -Dfile=htmlparser-1.6.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=httpclient-4.1.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=httpclient-4.1.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=httpclient-cache-4.1.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=httpclient-cache-4.1.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=httpcore-4.1.3 -Dversion=4.0.d -Dpackaging=jar -Dfile=httpcore-4.1.3.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=icu4j_3_6_1 -Dversion=4.0.d -Dpackaging=jar -Dfile=icu4j_3_6_1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jackson-core-asl-1.8.3 -Dversion=4.0.d -Dpackaging=jar -Dfile=jackson-core-asl-1.8.3.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jackson-mapper-asl-1.8.3 -Dversion=4.0.d -Dpackaging=jar -Dfile=jackson-mapper-asl-1.8.3.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jakarta-oro-2.0.8 -Dversion=4.0.d -Dpackaging=jar -Dfile=jakarta-oro-2.0.8.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jaxb-api-2.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=jaxb-api-2.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jaxb-impl-2.1.11 -Dversion=4.0.d -Dpackaging=jar -Dfile=jaxb-impl-2.1.11.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jaxb-xjc-2.1.7 -Dversion=4.0.d -Dpackaging=jar -Dfile=jaxb-xjc-2.1.7.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jaxen-1.1-beta-8 -Dversion=4.0.d -Dpackaging=jar -Dfile=jaxen-1.1-beta-8.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jaxrpc -Dversion=4.0.d -Dpackaging=jar -Dfile=jaxrpc.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jaxws-api-2.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=jaxws-api-2.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jaxws-rt-2.1.7 -Dversion=4.0.d -Dpackaging=jar -Dfile=jaxws-rt-2.1.7.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jbpm-identity-3.3.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=jbpm-identity-3.3.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jbpm-jpdl-3.3.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=jbpm-jpdl-3.3.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jcr-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=jcr-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jempbox-1.6.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=jempbox-1.6.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jgroups-2.11.1.Final -Dversion=4.0.d -Dpackaging=jar -Dfile=jgroups-2.11.1.Final.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jibx-bind -Dversion=4.0.d -Dpackaging=jar -Dfile=jibx-bind.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jibx-run -Dversion=4.0.d -Dpackaging=jar -Dfile=jibx-run.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jid3lib-0.5.4 -Dversion=4.0.d -Dpackaging=jar -Dfile=jid3lib-0.5.4.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=JMagick -Dversion=4.0.d -Dpackaging=jar -Dfile=JMagick.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=joda-time-1.2.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=joda-time-1.2.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jooconverter-2.1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=jooconverter-2.1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=JSlideShare-0.6 -Dversion=4.0.d -Dpackaging=jar -Dfile=JSlideShare-0.6.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=json-simple-1.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=json-simple-1.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=json -Dversion=4.0.d -Dpackaging=jar -Dfile=json.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jsr107cache-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=jsr107cache-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jsr181-api-1.0-MR1 -Dversion=4.0.d -Dpackaging=jar -Dfile=jsr181-api-1.0-MR1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jsr250-api-1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=jsr250-api-1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jstl-1.1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=jstl-1.1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jta -Dversion=4.0.d -Dpackaging=jar -Dfile=jta.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jtds-1.2.5 -Dversion=4.0.d -Dpackaging=jar -Dfile=jtds-1.2.5.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jug-asl-2.0.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=jug-asl-2.0.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=junit-4.8.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=junit-4.8.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jut -Dversion=4.0.d -Dpackaging=jar -Dfile=jut.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=jutf7-1.0.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=jutf7-1.0.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=livetribe-jsr223-2.0.6 -Dversion=4.0.d -Dpackaging=jar -Dfile=livetribe-jsr223-2.0.6.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=log4j-1.2.15 -Dversion=4.0.d -Dpackaging=jar -Dfile=log4j-1.2.15.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=lucene-analyzers-2.4.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=lucene-analyzers-2.4.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=lucene-core-2.4.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=lucene-core-2.4.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=lucene-regex-2.4.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=lucene-regex-2.4.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=lucene-snowball-2.4.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=lucene-snowball-2.4.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=mail -Dversion=4.0.d -Dpackaging=jar -Dfile=mail.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=mimepull-1.3 -Dversion=4.0.d -Dpackaging=jar -Dfile=mimepull-1.3.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=mockito-all-1.8.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=mockito-all-1.8.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=mybatis-3.0.4-patched -Dversion=4.0.d -Dpackaging=jar -Dfile=mybatis-3.0.4-patched.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=mybatis-spring-1.0.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=mybatis-spring-1.0.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=myfaces-api-1.1.8 -Dversion=4.0.d -Dpackaging=jar -Dfile=myfaces-api-1.1.8.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=myfaces-impl-1.1.8 -Dversion=4.0.d -Dpackaging=jar -Dfile=myfaces-impl-1.1.8.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=neethi-2.0.4 -Dversion=4.0.d -Dpackaging=jar -Dfile=neethi-2.0.4.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=netcdf-4.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=netcdf-4.2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=odmg-3.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=odmg-3.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=ooxml-schemas-1.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=ooxml-schemas-1.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=openoffice-juh-3.1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=openoffice-juh-3.1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=openoffice-jurt-3.1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=openoffice-jurt-3.1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=openoffice-ridl-3.1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=openoffice-ridl-3.1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=openoffice-sandbox-2.0.3 -Dversion=4.0.d -Dpackaging=jar -Dfile=openoffice-sandbox-2.0.3.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=openoffice-unoil-3.1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=openoffice-unoil-3.1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=opensaml-1.0.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=opensaml-1.0.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.aop-3.0.5.RELEASE -Dversion=4.0.d -Dpackaging=jar -Dfile=org.springframework.aop-3.0.5.RELEASE.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.asm-3.0.5.RELEASE -Dversion=4.0.d -Dpackaging=jar -Dfile=org.springframework.asm-3.0.5.RELEASE.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.beans-3.0.5.RELEASE -Dversion=4.0.d -Dpackaging=jar -Dfile=org.springframework.beans-3.0.5.RELEASE.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.context-3.0.5.RELEASE -Dversion=4.0.d -Dpackaging=jar -Dfile=org.springframework.context-3.0.5.RELEASE.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.context.support-3.0.5.RELEASE -Dversion=4.0.d -Dpackaging=jar -Dfile=org.springframework.context.support-3.0.5.RELEASE.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.core-3.0.5.RELEASE -Dversion=4.0.d -Dpackaging=jar -Dfile=org.springframework.core-3.0.5.RELEASE.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.expression-3.0.5.RELEASE -Dversion=4.0.d -Dpackaging=jar -Dfile=org.springframework.expression-3.0.5.RELEASE.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.jdbc-3.0.5.RELEASE -Dversion=4.0.d -Dpackaging=jar -Dfile=org.springframework.jdbc-3.0.5.RELEASE.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.orm-3.0.5.RELEASE -Dversion=4.0.d -Dpackaging=jar -Dfile=org.springframework.orm-3.0.5.RELEASE.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.transaction-3.0.5.RELEASE -Dversion=4.0.d -Dpackaging=jar -Dfile=org.springframework.transaction-3.0.5.RELEASE.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.web-3.0.5.RELEASE -Dversion=4.0.d -Dpackaging=jar -Dfile=org.springframework.web-3.0.5.RELEASE.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.web.servlet-3.0.5.RELEASE -Dversion=4.0.d -Dpackaging=jar -Dfile=org.springframework.web.servlet-3.0.5.RELEASE.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=pdfbox-1.6.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=pdfbox-1.6.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=PDFRenderer-0.9.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=PDFRenderer-0.9.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=poi-3.8-beta5-20111128 -Dversion=4.0.d -Dpackaging=jar -Dfile=poi-3.8-beta5-20111128.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=poi-ooxml-3.8-beta5-20111128 -Dversion=4.0.d -Dpackaging=jar -Dfile=poi-ooxml-3.8-beta5-20111128.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=poi-scratchpad-3.8-beta5-20111128 -Dversion=4.0.d -Dpackaging=jar -Dfile=poi-scratchpad-3.8-beta5-20111128.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=quartz-1.8.3-patched -Dversion=4.0.d -Dpackaging=jar -Dfile=quartz-1.8.3-patched.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=resolver-20050927 -Dversion=4.0.d -Dpackaging=jar -Dfile=resolver-20050927.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=rhino-js-1.6R7 -Dversion=4.0.d -Dpackaging=jar -Dfile=rhino-js-1.6R7.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=rome-0.9 -Dversion=4.0.d -Dpackaging=jar -Dfile=rome-0.9.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=saxpath -Dversion=4.0.d -Dpackaging=jar -Dfile=saxpath.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=shale-test-1.0.4 -Dversion=4.0.d -Dpackaging=jar -Dfile=shale-test-1.0.4.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=slf4j-api-1.5.11 -Dversion=4.0.d -Dpackaging=jar -Dfile=slf4j-api-1.5.11.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=slf4j-log4j12-1.5.11 -Dversion=4.0.d -Dpackaging=jar -Dfile=slf4j-log4j12-1.5.11.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=spring-cmis-framework-1.0.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=spring-cmis-framework-1.0.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=spring-security-core-3.1.0.RC2 -Dversion=4.0.d -Dpackaging=jar -Dfile=spring-security-core-3.1.0.RC2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=spring-social-core-1.0.0.RC1 -Dversion=4.0.d -Dpackaging=jar -Dfile=spring-social-core-1.0.0.RC1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=spring-social-facebook-1.0.0.RC1 -Dversion=4.0.d -Dpackaging=jar -Dfile=spring-social-facebook-1.0.0.RC1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=spring-social-facebook-web-1.0.0.RC1 -Dversion=4.0.d -Dpackaging=jar -Dfile=spring-social-facebook-web-1.0.0.RC1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=spring-social-linkedin-1.0.0.BUILD-SNAPSHOT -Dversion=4.0.d -Dpackaging=jar -Dfile=spring-social-linkedin-1.0.0.BUILD-SNAPSHOT.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=spring-social-test-1.0.0.RC1 -Dversion=4.0.d -Dpackaging=jar -Dfile=spring-social-test-1.0.0.RC1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=spring-social-twitter-1.0.0.RC1 -Dversion=4.0.d -Dpackaging=jar -Dfile=spring-social-twitter-1.0.0.RC1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=spring-social-web-1.0.0.RC1 -Dversion=4.0.d -Dpackaging=jar -Dfile=spring-social-web-1.0.0.RC1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=spring-surf-1.0.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=spring-surf-1.0.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=spring-surf-api-1.0.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=spring-surf-api-1.0.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=spring-surf-core-1.0.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=spring-surf-core-1.0.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=spring-surf-core-configservice-1.0.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=spring-surf-core-configservice-1.0.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=spring-webscripts-1.0.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=spring-webscripts-1.0.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=spring-webscripts-api-1.0.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=spring-webscripts-api-1.0.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=springmodules-jbpm31 -Dversion=4.0.d -Dpackaging=jar -Dfile=springmodules-jbpm31.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=standard -Dversion=4.0.d -Dpackaging=jar -Dfile=standard.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=stax-api-1.0.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=stax-api-1.0.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=stax-ex-1.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=stax-ex-1.2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=stax-utils-20060502 -Dversion=4.0.d -Dpackaging=jar -Dfile=stax-utils-20060502.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=streambuffer-0.9 -Dversion=4.0.d -Dpackaging=jar -Dfile=streambuffer-0.9.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=subethasmtp-3.1.6 -Dversion=4.0.d -Dpackaging=jar -Dfile=subethasmtp-3.1.6.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=tagsoup-1.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=tagsoup-1.2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=tika-core-1.1-20111128 -Dversion=4.0.d -Dpackaging=jar -Dfile=tika-core-1.1-20111128.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=tika-parsers-1.1-20111128 -Dversion=4.0.d -Dpackaging=jar -Dfile=tika-parsers-1.1-20111128.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=truezip -Dversion=4.0.d -Dpackaging=jar -Dfile=truezip.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=urlrewritefilter-3.1.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=urlrewritefilter-3.1.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=vorbis-java-core-0.1-SNAPSHOT -Dversion=4.0.d -Dpackaging=jar -Dfile=vorbis-java-core-0.1-SNAPSHOT.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=vorbis-java-tika-0.1-SNAPSHOT -Dversion=4.0.d -Dpackaging=jar -Dfile=vorbis-java-tika-0.1-SNAPSHOT.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=wsdl4j-1.6.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=wsdl4j-1.6.2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=wss4j-1.5.4-patched -Dversion=4.0.d -Dpackaging=jar -Dfile=wss4j-1.5.4-patched.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=wstx-asl-3.2.4 -Dversion=4.0.d -Dpackaging=jar -Dfile=wstx-asl-3.2.4.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=xercesImpl-2.8.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=xercesImpl-2.8.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=xml-resolver-1.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=xml-resolver-1.2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=xmlbeans-2.3.0 -Dversion=4.0.d -Dpackaging=jar -Dfile=xmlbeans-2.3.0.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=xmlgraphics-commons-1.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=xmlgraphics-commons-1.2.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=xmlrpc -Dversion=4.0.d -Dpackaging=jar -Dfile=xmlrpc.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=XmlSchema-1.4.5 -Dversion=4.0.d -Dpackaging=jar -Dfile=XmlSchema-1.4.5.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=xmlsec-1.4.1 -Dversion=4.0.d -Dpackaging=jar -Dfile=xmlsec-1.4.1.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=xpp3-1.1.3_8 -Dversion=4.0.d -Dpackaging=jar -Dfile=xpp3-1.1.3_8.jar
mvn install:install-file -Dclassifier=community  -DgroupId=org.alfresco.sdk -DartifactId=xstream-1.2.2 -Dversion=4.0.d -Dpackaging=jar -Dfile=xstream-1.2.2.jar

echo "Additional libs"

cd $BOOTSTRAP_HOME/alfresco-sdk/lib

mvn install:install-file  -DgroupId=org.alfresco.sdk -DartifactId=config -Dversion=4.0.d -Dclassifier=community  -Dpackaging=jar -Dfile=config.jar
mvn install:install-file  -DgroupId=org.alfresco.sdk -DartifactId=config-webscript -Dversion=4.0.d -Dclassifier=community  -Dpackaging=jar -Dfile=config-webscript.jar
cd ..
mvn install

echo "birt libs"

cd $BOOTSTRAP_HOME/birt-sdk/lib


sh mavenize.sh
cd ..
mvn install




