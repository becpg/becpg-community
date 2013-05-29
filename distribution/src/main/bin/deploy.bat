@echo off
rem -------
rem Deploy beCPG AMPS's scripts
rem -------

if [%1]==[] goto usage

set SERVER=%1
set DEPLOY_ROOT=%SERVER%\..\..\deploy

set /p ansreport=Deploy report server? (y/n):

@echo "**********************************************************"
@echo "Deploy core AMP"
@echo "**********************************************************"

del /Q %SERVER%\webapps\alfresco.war
rmdir /S /Q %SERVER%\webapps\alfresco
xcopy %SERVER%\webapps\alfresco.war.setup %SERVER%\webapps\alfresco.war


"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  
amps\alfresco-core-patch-*.amp %SERVER%\webapps\alfresco.war -force -
nobackup 
"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  
amps\becpg-controls-core-*.amp  %SERVER%\webapps\alfresco.war -force -
nobackup
"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  
amps\becpg-designer-core-*.amp  %SERVER%\webapps\alfresco.war -force -
nobackup
"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  
amps\becpg-core-*.amp %SERVER%\webapps\alfresco.war -force -nobackup
"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  
amps\alfresco-googledocs-repo-*.amp %SERVER%\webapps\alfresco.war -
force -nobackup


@echo "**********************************************************"
@echo "Deploy share AMP"
@echo "**********************************************************"

del /Q %SERVER%\webapps\share.war
rmdir /S /Q %SERVER%\webapps\share
xcopy %SERVER%\webapps\share.war.setup %SERVER%\webapps\share.war

"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  
amps\becpg-controls-share-*.amp %SERVER%\webapps\share.war -force -
nobackup 
"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  
amps\becpg-designer-share-*.amp %SERVER%\webapps\share.war -force -
nobackup 
"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  
amps\becpg-share-*.amp %SERVER%\webapps\share.war -force -nobackup 
"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  
amps\alfresco-googledocs-share-*.amp %SERVER%\webapps\share.war -force 
-nobackup 

rmdir /S /Q %SERVER%/temp/*
rmdir /S /Q %SERVER%/work/*
del %SERVER%/webapps/*.bak


@echo "**********************************************************"
@echo "Deploy patch "
@echo "**********************************************************"

%JAVA_HOME%\bin\jar ufv %SERVER%/webapps/share.war -C dist/share .


if /i "%ansreport%"=="y" goto report


goto :eof
:usage 
@echo Usage: %0 ^<server path^>
exit /B 1

:report
echo "**********************************************************"
echo "Deploy Report Server"
echo "**********************************************************"


rmdir /S /Q %SERVER%\webapps\becpg-report
xcopy amps\becpg-report-*.war %SERVER%\webapps\becpg-report.war


