@REM
@REM Copyright (C) 2018 ZTE, Inc. and others. All rights reserved. (ZTE)
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM         http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM


@echo off

set RUNHOME=%~dp0%
echo ### RUNHOME: %RUNHOME%

if exist "%RUNHOME%setenv.bat" (
  call "%RUNHOME%setenv.bat"
)


echo ================== ENV_INFO  =============================================
echo RUNHOME=%RUNHOME%
echo JAVA_BASE=%JAVA_BASE%
echo Main_Class=%Main_Class%
echo APP_INFO=%APP_INFO%
echo Main_JAR=%Main_JAR%
echo Main_Conf=%Main_Conf%
echo ==========================================================================

title %APP_INFO%
echo ### Starting %APP_INFO%

IF EXIST "%JAVA_BASE%" (
set JAVA_HOME=%JAVA_BASE%
)

set JAVA="%JAVA_HOME%/bin/java"
set JAVA_OPTS=-Xms50m -Xmx128m
set port=8778

setlocal enabledelayedexpansion
set index=1
FOR /F "tokens=3 delims= " %%a in ('"%JAVA% -version 2>&1"')do (
  if !index! equ 1 set JAVA_VERSION=%%a
  set /a index=index+1
)
set JAVA_VERSION=%JAVA_VERSION:~1,3%
if "%JAVA_VERSION%"=="1.8" (
   set jvm_opts=-Xms16m -Xmx128m -XX:+UseSerialGC -XX:MaxMetaspaceSize=64m  -XX:NewRatio=2 
) else (
   set jvm_opts=-Xms16m -Xmx128m -XX:+UseSerialGC  -XX:MaxPermSize=64m -XX:NewRatio=2
)

set jvm_opts=%jvm_opts% -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=%port%,server=y,suspend=n

set CLASS_PATH=%RUNHOME%;%RUNHOME%%Main_JAR%


echo ================== RUN_INFO  =============================================
echo ### JAVA_HOME: %JAVA_HOME%
echo ### JAVA: %JAVA%
echo ### jvm_opts: %jvm_opts%
echo ### class_path: %CLASS_PATH%
echo ### EXT_DIRS: %EXT_DIRS%
echo ==========================================================================

cd /d %RUNHOME%

echo @JAVA@ %JAVA%
echo @JAVA_CMD@
%JAVA% -classpath %CLASS_PATH% %jvm_opts% %Main_Class% server %RUNHOME%/%Main_Conf%

IF ERRORLEVEL 1 goto showerror
exit
:showerror
echo WARNING: Error occurred during startup or Server abnormally stopped by way of killing the process,Please check!
echo After checking, press any key to close 
pause
exit
