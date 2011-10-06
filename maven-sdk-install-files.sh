#!/bin/sh
. ./common.sh

echo "Install alfresco libs"


cd $ALF_SDK/lib/server/

mvn install:install-file -Dfile=dependencies/spring-surf/spring-surf-1.0.0.CI-SNAPSHOT.jar -DgroupId=org.alfresco.sdk -DartifactId=spring-surf-1.0.0.CI-SNAPSHOT -Dversion=3.4 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=dependencies/spring-surf/spring-webscripts-1.0.0.CI-SNAPSHOT-tests.jar -DgroupId=org.alfresco.sdk -DartifactId=spring-webscripts-1.0.0.CI-SNAPSHOT-tests -Dversion=3.4 -Dpackaging=jar  -DgeneratePom=true
mvn install:install-file -Dfile=dependencies/spring-surf/spring-webscripts-1.0.0.CI-SNAPSHOT.jar -DgroupId=org.alfresco.sdk -DartifactId=spring-webscripts-1.0.0.CI-SNAPSHOT -Dversion=3.4 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=dependencies/spring-surf/spring-webscripts-api-1.0.0.CI-SNAPSHOT.jar -DgroupId=org.alfresco.sdk -DartifactId=spring-webscripts-api-1.0.0.CI-SNAPSHOT -Dversion=3.4 -Dpackaging=jar -DgeneratePom=true


cd $SERVER/webapps/alfresco/WEB-INF/lib/
 
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=abdera-client-0.4.0-incubating -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=abdera-client-0.4.0-incubating.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=abdera-core-0.4.0-incubating -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=abdera-core-0.4.0-incubating.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=abdera-extensions-json-0.4.0-incubating -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=abdera-extensions-json-0.4.0-incubating.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=abdera-i18n-0.4.0-incubating -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=abdera-i18n-0.4.0-incubating.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=abdera-parser-0.4.0-incubating -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=abdera-parser-0.4.0-incubating.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=acegi-security-0.8.2_patched -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=acegi-security-0.8.2_patched.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=activation -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=activation.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=addressing-1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=addressing-1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=alfresco-core -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=alfresco-core-3.4.d.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=alfresco-data-model -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=alfresco-data-model-3.4.d.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=alfresco-deployment -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=alfresco-deployment-3.4.d.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=alfresco-jlan-embed -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=alfresco-jlan-embed-3.4.d.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=alfresco-mbeans -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=alfresco-mbeans-3.4.d.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=alfresco-remote-api -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=alfresco-remote-api-3.4.d.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=alfresco-repository -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=alfresco-repository-3.4.d.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=alfresco-wdr-deployment -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=alfresco-wdr-deployment.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=alfresco-web-client -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=alfresco-web-client-3.4.d.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=alfresco-web-framework-commons -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=alfresco-web-framework-commons-3.4.d.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=ant-1.7.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=ant-1.7.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=antlr-3.2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=antlr-3.2.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=aopalliance -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=aopalliance.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=asm-3.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=asm-3.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=avalon-framework-4.2.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=avalon-framework-4.2.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=axiom-api-1.2.5 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=axiom-api-1.2.5.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=axiom-impl-1.2.5 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=axiom-impl-1.2.5.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=axis-1.4 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=axis-1.4.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=batik-all-1.6 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=batik-all-1.6.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=bcel -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=bcel.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=bcmail-jdk15-1.45 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=bcmail-jdk15-1.45.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=bcprov-jdk15-1.45 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=bcprov-jdk15-1.45.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=bliki-3.0.2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=bliki-3.0.2.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=bsf-2.4.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=bsf-2.4.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=bsh-1.3.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=bsh-1.3.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=cglib-2.2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=cglib-2.2.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=chemistry-abdera-0.1-incubating-SNAPSHOT -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=chemistry-abdera-0.1-incubating-SNAPSHOT.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=chemistry-tck-atompub-0.1-incubating-SNAPSHOT -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=chemistry-tck-atompub-0.1-incubating-SNAPSHOT.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=chiba-1.3.0-patched -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=chiba-1.3.0-patched.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-beanutils-1.7.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-beanutils-1.7.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-codec-1.3 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-codec-1.3.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-collections-3.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-collections-3.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-compress-1.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-compress-1.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-dbcp-1.2.2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-dbcp-1.2.2.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-digester-1.6 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-digester-1.6.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-discovery-0.2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-discovery-0.2.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-el -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-el.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-fileupload-1.2.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-fileupload-1.2.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-httpclient-3.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-httpclient-3.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-io-1.4 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-io-1.4.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-jxpath-1.2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-jxpath-1.2.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-lang-2.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-lang-2.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-logging-1.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-logging-1.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-modeler -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-modeler.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-pool-1.4 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-pool-1.4.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=commons-validator -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=commons-validator.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=cxf-2.2.2-patched -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=cxf-2.2.2-patched.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=dom4j-1.6.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=dom4j-1.6.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=drew-image-metadata-extractor-2.4.0-beta-1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=drew-image-metadata-extractor-2.4.0-beta-1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=ehcache-core-2.0.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=ehcache-core-2.0.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=facebook_070716 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=facebook_070716.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=FastInfoset-1.2.2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=FastInfoset-1.2.2.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=fontbox-1.2.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=fontbox-1.2.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=fop-0.94 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=fop-0.94.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=freemarker-2.3.16-patched -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=freemarker-2.3.16-patched.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=gdata-client-1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=gdata-client-1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=gdata-core-1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=gdata-core-1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=gdata-docs-3.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=gdata-docs-3.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=gdata-media-1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=gdata-media-1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=geronimo-activation_1.1_spec-1.0.2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=geronimo-activation_1.1_spec-1.0.2.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=geronimo-annotation_1.0_spec-1.1.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=geronimo-annotation_1.0_spec-1.1.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=geronimo-jaxws_2.1_spec-1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=geronimo-jaxws_2.1_spec-1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=geronimo-stax-api_1.0_spec-1.0.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=geronimo-stax-api_1.0_spec-1.0.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=geronimo-ws-metadata_2.0_spec-1.1.2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=geronimo-ws-metadata_2.0_spec-1.1.2.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=google-collect-1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=google-collect-1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=greenmail-1.3-patched -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=greenmail-1.3-patched.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=guessencoding-1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=guessencoding-1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=hibernate-3.2.6-patched -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=hibernate-3.2.6-patched.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=hrtlib -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=hrtlib.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=htmlparser-1.6 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=htmlparser-1.6.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=ibatis-2.3.4.726-patched -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=ibatis-2.3.4.726-patched.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=icu4j_3_6_1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=icu4j_3_6_1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jakarta-oro-2.0.8 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jakarta-oro-2.0.8.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jaxb-api-2.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jaxb-api-2.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jaxb-impl-2.1.7 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jaxb-impl-2.1.7.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jaxb-xjc-2.1.7 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jaxb-xjc-2.1.7.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jaxen-1.1-beta-8 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jaxen-1.1-beta-8.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jaxrpc -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jaxrpc.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jaxws-api-2.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jaxws-api-2.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jbpm-identity-3.3.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jbpm-identity-3.3.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jbpm-jpdl-3.3.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jbpm-jpdl-3.3.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jcr-1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jcr-1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jgroups-2.8.0-b2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jgroups-2.8.0-b2.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jibx-bind -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jibx-bind.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jibx-run -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jibx-run.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jid3lib-0.5.4 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jid3lib-0.5.4.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=JMagick -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=JMagick.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=joda-time-1.2.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=joda-time-1.2.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jooconverter-2.1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jooconverter-2.1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=json -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=json.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jsr107cache-1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jsr107cache-1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jstl-1.1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jstl-1.1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jta -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jta.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jtds-1.2.5 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jtds-1.2.5.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jug-asl-2.0.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jug-asl-2.0.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=junit-4.6 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=junit-4.6.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jut -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jut.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=jutf7-1.0.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=jutf7-1.0.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=log4j-1.2.15 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=log4j-1.2.15.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=lucene-analyzers-2.4.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=lucene-analyzers-2.4.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=lucene-core-2.4.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=lucene-core-2.4.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=lucene-regex-2.4.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=lucene-regex-2.4.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=lucene-snowball-2.4.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=lucene-snowball-2.4.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=mail -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=mail.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=mockito-all-1.8.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=mockito-all-1.8.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=myfaces-api-1.1.5 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=myfaces-api-1.1.5.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=myfaces-impl-1.1.5 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=myfaces-impl-1.1.5.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=neethi-2.0.4 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=neethi-2.0.4.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=netcdf-java-4.0.41 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=netcdf-java-4.0.41.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=odmg-3.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=odmg-3.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=ooxml-schemas-1.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=ooxml-schemas-1.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=opencmis-test-browser-0.1-SNAPSHOT -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=opencmis-test-browser-0.1-SNAPSHOT.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=openoffice-juh-3.1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=openoffice-juh-3.1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=openoffice-jurt-3.1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=openoffice-jurt-3.1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=openoffice-ridl-3.1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=openoffice-ridl-3.1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=openoffice-sandbox-2.0.3 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=openoffice-sandbox-2.0.3.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=openoffice-unoil-3.1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=openoffice-unoil-3.1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=opensaml-1.0.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=opensaml-1.0.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.aop-3.0.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=org.springframework.aop-3.0.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.asm-3.0.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=org.springframework.asm-3.0.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.beans-3.0.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=org.springframework.beans-3.0.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.context-3.0.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=org.springframework.context-3.0.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.context.support-3.0.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=org.springframework.context.support-3.0.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.core-3.0.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=org.springframework.core-3.0.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.expression-3.0.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=org.springframework.expression-3.0.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.jdbc-3.0.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=org.springframework.jdbc-3.0.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.orm-3.0.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=org.springframework.orm-3.0.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.transaction-3.0.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=org.springframework.transaction-3.0.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.web-3.0.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=org.springframework.web-3.0.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=org.springframework.webmvc-3.0.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=org.springframework.webmvc-3.0.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=pdfbox-1.2.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=pdfbox-1.2.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=PDFRenderer-2009-09-27 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=PDFRenderer-2009-09-27.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=poi-3.7-beta3-20100915 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=poi-3.7-beta3-20100915.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=poi-ooxml-3.7-beta3-20100915 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=poi-ooxml-3.7-beta3-20100915.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=poi-scratchpad-3.7-beta3-20100915 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=poi-scratchpad-3.7-beta3-20100915.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=postgresql-8.4-701.jdbc3 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=postgresql-8.4-701.jdbc3.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=quartz-1.8.3 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=quartz-1.8.3.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=resolver -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=resolver.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=rhino-js-1.6R7 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=rhino-js-1.6R7.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=saxpath -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=saxpath.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=shale-test-1.0.4 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=shale-test-1.0.4.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=slf4j-api-1.5.11 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=slf4j-api-1.5.11.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=slf4j-log4j12-1.5.11 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=slf4j-log4j12-1.5.11.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=spring-surf-1.0.0.CI-SNAPSHOT -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=spring-surf-1.0.0.CI-SNAPSHOT.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=spring-surf-api-1.0.0.CI-SNAPSHOT -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=spring-surf-api-1.0.0.CI-SNAPSHOT.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=spring-surf-core-1.0.0.CI-SNAPSHOT -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=spring-surf-core-1.0.0.CI-SNAPSHOT.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=spring-surf-core-configservice-1.0.0.CI-SNAPSHOT -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=spring-surf-core-configservice-1.0.0.CI-SNAPSHOT.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=spring-webscripts-1.0.0.CI-SNAPSHOT -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=spring-webscripts-1.0.0.CI-SNAPSHOT.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=spring-webscripts-api-1.0.0.CI-SNAPSHOT -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=spring-webscripts-api-1.0.0.CI-SNAPSHOT.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=springmodules-jbpm31 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=springmodules-jbpm31.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=standard -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=standard.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=stax-api-1.0.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=stax-api-1.0.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=stax-utils-20060502 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=stax-utils-20060502.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=stringtemplate-3.2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=stringtemplate-3.2.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=subetha-smtp -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=subetha-smtp.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=tagsoup-1.2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=tagsoup-1.2.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=tika-core-0.8-SNAPSHOT -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=tika-core-0.8-SNAPSHOT.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=tika-parsers-0.8-SNAPSHOT -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=tika-parsers-0.8-SNAPSHOT.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=truezip -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=truezip.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=urlrewritefilter-3.1.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=urlrewritefilter-3.1.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=wsdl4j-1.6.2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=wsdl4j-1.6.2.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=wss4j-1.5.4-patched -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=wss4j-1.5.4-patched.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=wstx-asl-3.2.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=wstx-asl-3.2.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=wstx-asl-3.2.4 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=wstx-asl-3.2.4.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=xercesImpl-2.8.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=xercesImpl-2.8.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=xml-apis -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=xml-apis.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=xml-resolver-1.2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=xml-resolver-1.2.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=xmlbeans-2.3.0 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=xmlbeans-2.3.0.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=xmlgraphics-commons-1.2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=xmlgraphics-commons-1.2.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=xmlrpc -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=xmlrpc.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=XmlSchema-1.4.5 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=XmlSchema-1.4.5.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=xmlsec-1.4.1 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=xmlsec-1.4.1.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=xpp3-1.1.3_8 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=xpp3-1.1.3_8.jar
mvn install:install-file -q -DgroupId=org.alfresco.sdk -DartifactId=xstream-1.2.2 -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=xstream-1.2.2.jar

echo "Additional libs"

cd $BOOTSTRAP_HOME/alfresco-sdk/lib

mvn install:install-file -q  -DgroupId=org.alfresco.sdk -DartifactId=config -Dversion=3.4 -Dclassifier=community  -Dpackaging=jar -Dfile=config.jar
cd ..
mvn install

echo "birt libs"

cd $BOOTSTRAP_HOME/birt-sdk/lib


sh mavenize.sh
cd ..
mvn install




