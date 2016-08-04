#!/bin/bash

mvn clean
mvn org.zanata:zanata-maven-plugin:3.9.1:push -Dzanata.deleteObsoleteModules=true -Dzanata.pushType=both  -Dzanata.excludes='**/becpg-config-*.properties,**/test/**/*.properties,becpg-enterprise/**/*.properties,becpg-olap/**/*.properties,becpg-report/**/*.properties,**/dataTypeAnalyzers.properties,**/bin/**/*.properties,**/log4j.properties,**/module.properties,**/*/file-mapping.properties,**/*.metadata,**/target/**/*.properties,**/becpgVisibilityPropertiesModel.properties,**/alfresco-global.properties,**/application.properties,**/becpg-designer-share.properties,**/dev-log4j.properties,**/log4j-solr.properties,**/solrcore.properties,.hglf/**' 
