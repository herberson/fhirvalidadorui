@echo off

set CURRENT_DIR=%~dp0
set CLASSPATH=  
set LIBS_DIR=

IF EXIST "build.gradle" (
  set LIBS_DIR=%CURRENT_DIR%\build\libs
) ELSE (
  set LIBS_DIR=%CURRENT_DIR%
)

call :setall . %LIBS_DIR%

java -splash:%LIBS_DIR%\splash.gif -cp %CLASSPATH% ui.FhirValidacaoWindow

goto end
  
:setall
if .%1.==.. goto end
set dir=%1
set dir=%dir:"=%
if not "%CLASSPATH%"=="" set CLASSPATH=%CLASSPATH%;%dir%
if "%CLASSPATH%"=="" set CLASSPATH=%dir%
for %%i in ("%dir%\*.jar") do call :setone "%%i"
shift
goto setall
  
:setone
set file=%1
set file=%file:"=%
set CLASSPATH=%CLASSPATH%;%file%

:end
