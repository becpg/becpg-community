#!/bin/bash

mvn clean
mvn org.zanata:zanata-maven-plugin:3.3.2:push-module -Dzanata.deleteObsoleteModules=true -Dzanata.pushType=both  -Dzanata.excludes='**/becpg-config-*.properties,**/test/**/*.properties,becpg-enterprise/**/*.properties,becpg-olap/**/*.properties,becpg-report/**/*.properties,**/dataTypeAnalyzers.properties,**/bin/**/*.properties,**/log4j.properties,**/module.properties,**/*/file-mapping.properties,**/*.metadata,**/target/**/*.properties,**/becpgVisibilityPropertiesModel.properties' -Dzanata.disableSSLCert 
