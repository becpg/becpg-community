@echo off
rem -------
rem Deploy beCPG AMPS's scripts
rem -------

if [%1]==[] goto usage

set SERVER=%1
set DEPLOY_ROOT=%SERVER%\..\..\deploy

set /p ansreport=Deploy saiku? (y/n):

@echo "**********************************************************"
@echo "Deploy core AMP"
@echo "**********************************************************"

del /Q /F %SERVER%\webapps\alfresco.war
rmdir /S /Q %SERVER%\webapps\alfresco
xcopy %SERVER%\webapps\alfresco.war.setup %SERVER%\webapps\alfresco.war


"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  amps\alfresco-core-patch-*.amp %SERVER%\webapps\alfresco.war -force -nobackup 
"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  amps\becpg-controls-core-*.amp  %SERVER%\webapps\alfresco.war -force -nobackup
"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  amps\becpg-designer-core-*.amp  %SERVER%\webapps\alfresco.war -force -nobackup
"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  amps\becpg-core-*.amp %SERVER%\webapps\alfresco.war -force -nobackup



@echo "**********************************************************"
@echo "Deploy share AMP"
@echo "**********************************************************"

del /Q /F %SERVER%\webapps\share.war
rmdir /S /Q %SERVER%\webapps\share
xcopy %SERVER%\webapps\share.war.setup %SERVER%\webapps\share.war

"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  amps\becpg-controls-share-*.amp %SERVER%\webapps\share.war -force -nobackup 
"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  amps\becpg-designer-share-*.amp %SERVER%\webapps\share.war -force -nobackup 
"%JAVA_HOME%\bin\java" -jar "%DEPLOY_ROOT%\alfresco-mmt.jar" install  amps\becpg-share-*.amp %SERVER%\webapps\share.war -force -nobackup 


@echo "**********************************************************"
@echo "Deploy patch "
@echo "**********************************************************"

%JAVA_HOME%\bin\jar ufv %SERVER%/webapps/share.war -C dist/share .


if /i "%anssaiku%"=="y" goto saiku

goto :eof
:usage 
@echo Usage: %0 ^<server path^>
exit /B 1


:saiku
@echo "**********************************************************"
@echo "Deploy OLAP Cube"
@echo "**********************************************************"
del /Q %SERVER%\webapps\saiku.war
del /Q %SERVER%\webapps\saiku-ui.war
rmdir /S /Q %SERVER%\webapps\saiku
rmdir /S /Q %SERVER%\webapps\saiku-ui
xcopy %SERVER%\webapps\saiku.war.setup %SERVER%\webapps\saiku.war
xcopy %SERVER%\webapps\saiku-ui.war.setup %SERVER%\webapps\saiku-ui.war

"%JAVA_HOME%\bin\jar" ufv %SERVER%\webapps\saiku.war -C dist\saiku .
"%JAVA_HOME%\bin\jar" ufv %SERVER%\webapps\saiku-ui.war -C dist\saiku-ui .

