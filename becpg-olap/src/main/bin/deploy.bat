@echo off
rem -------
rem Deploy beCPG Olap scripts
rem -------

if [%1]==[] goto usage

set SERVER=%1

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


goto :eof
:usage 
@echo Usage: %0 ^<server path^>
exit /B 1