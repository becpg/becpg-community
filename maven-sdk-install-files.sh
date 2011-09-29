#!/bin/sh
. ./common.sh

echo "install alfresco jar"

cd $ALF_SDK/lib/server/

mvn install:install-file -Dfile=alfresco-repository-3.4.d.jar	-DgroupId=org.alfresco.sdk -DartifactId=alfresco-repository -Dversion=3.4 -Dpackaging=jar -Dclassifier=community -DgeneratePom=true
mvn install:install-file -Dfile=alfresco-core-3.4.d.jar 	-DgroupId=org.alfresco.sdk -DartifactId=alfresco-core -Dversion=3.4 -Dpackaging=jar -Dclassifier=community -DgeneratePom=true
mvn install:install-file -Dfile=alfresco-web-client-3.4.d.jar 	-DgroupId=org.alfresco.sdk -DartifactId=alfresco-web-client -Dversion=3.4 -Dpackaging=jar -Dclassifier=community -DgeneratePom=true
mvn install:install-file -Dfile=alfresco-repository-3.4.d.jar 	-DgroupId=org.alfresco.sdk -DartifactId=alfresco-repository -Dversion=3.4 -Dpackaging=jar -Dclassifier=community -DgeneratePom=true
mvn install:install-file -Dfile=alfresco-remote-api-3.4.d.jar 	-DgroupId=org.alfresco.sdk -DartifactId=alfresco-remote-api -Dversion=3.4 -Dpackaging=jar -Dclassifier=community -DgeneratePom=true
mvn install:install-file -Dfile=alfresco-data-model-3.4.d.jar 	-DgroupId=org.alfresco.sdk -DartifactId=alfresco-data-model -Dversion=3.4 -Dpackaging=jar -Dclassifier=community -DgeneratePom=true

echo "install surf artifacts"

mvn install:install-file -Dfile=dependencies/spring-surf/spring-surf-1.0.0.CI-SNAPSHOT.jar -DgroupId=org.alfresco.sdk -DartifactId=spring-surf-1.0.0.CI-SNAPSHOT -Dversion=3.4 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=dependencies/spring-surf/spring-webscripts-1.0.0.CI-SNAPSHOT-tests.jar -DgroupId=org.alfresco.sdk -DartifactId=spring-webscripts-1.0.0.CI-SNAPSHOT-tests -Dversion=3.4 -Dpackaging=jar  -DgeneratePom=true
mvn install:install-file -Dfile=dependencies/spring-surf/spring-webscripts-1.0.0.CI-SNAPSHOT.jar -DgroupId=org.alfresco.sdk -DartifactId=spring-webscripts-1.0.0.CI-SNAPSHOT -Dversion=3.4 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=dependencies/spring-surf/spring-webscripts-api-1.0.0.CI-SNAPSHOT.jar -DgroupId=org.alfresco.sdk -DartifactId=spring-webscripts-api-1.0.0.CI-SNAPSHOT -Dversion=3.4 -Dpackaging=jar -DgeneratePom=true

echo "jbpm"

mvn install:install-file -Dfile=dependencies/jbpm/jbpm-jpdl-3.3.1.jar -DgroupId=org.alfresco.sdk -DartifactId=jbpm-jpdl -Dversion=3.3.1 -Dpackaging=jar -DgeneratePom=true

echo "additional libs"

cd $BOOTSTRAP_HOME/lib
sh mavenize.sh

