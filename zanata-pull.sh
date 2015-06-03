#!/bin/bash

mvn clean
mvn org.zanata:zanata-maven-plugin:3.3.2:pull-module -Dzanata.locales=de,es,ru,nl,it -Dzanata.disableSSLCert 
