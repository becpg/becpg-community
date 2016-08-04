#!/bin/bash

mvn clean
mvn org.zanata:zanata-maven-plugin:3.9.1:pull-module -Dzanata.locales=de,es,ru,nl,it,pt -Dzanata.disableSSLCert 
