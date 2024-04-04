@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  WorkShiftly startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and WORK_SHIFTLY_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Djava.util.logging.config.file=./logging.properties" "-Dsentry.release=unspecified" "-Dsentry.environment=production" "-Dsentry.tags=OS:windows"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\WorkShiftly.jar;%APP_HOME%\lib\jfoenix-9.0.10.jar;%APP_HOME%\lib\ini4j-0.5.4.jar;%APP_HOME%\lib\ormlite-jdbc-5.1.jar;%APP_HOME%\lib\ormlite-core-5.1.jar;%APP_HOME%\lib\sqlite-jdbc-3.35.5.3.jar;%APP_HOME%\lib\jnativehook-2.1.0.jar;%APP_HOME%\lib\azure-identity-1.11.1.jar;%APP_HOME%\lib\msal4j-persistence-extension-1.2.0.jar;%APP_HOME%\lib\jna-platform-5.13.0.jar;%APP_HOME%\lib\jna-5.13.0.jar;%APP_HOME%\lib\unirest-object-mappers-gson-3.7.02.jar;%APP_HOME%\lib\commons-validator-1.6.jar;%APP_HOME%\lib\javafaker-1.0.2.jar;%APP_HOME%\lib\commons-lang3-3.11.jar;%APP_HOME%\lib\joda-time-2.10.6.jar;%APP_HOME%\lib\junique-1.0.4.jar;%APP_HOME%\lib\java-dotenv-5.2.1.jar;%APP_HOME%\lib\mvvmfx-1.8.0.jar;%APP_HOME%\lib\sentry-1.7.30.jar;%APP_HOME%\lib\slf4j-simple-1.7.30.jar;%APP_HOME%\lib\microsoft-graph-3.0.0.jar;%APP_HOME%\lib\microsoft-graph-core-3.1.0.jar;%APP_HOME%\lib\microsoft-kiota-authentication-azure-1.0.0.jar;%APP_HOME%\lib\azure-core-http-netty-1.13.11.jar;%APP_HOME%\lib\azure-core-1.46.0.jar;%APP_HOME%\lib\msal4j-1.14.0.jar;%APP_HOME%\lib\slf4j-api-1.7.36.jar;%APP_HOME%\lib\rxjava-3.0.7.jar;%APP_HOME%\lib\google-api-client-1.31.0.jar;%APP_HOME%\lib\unirest-java-3.7.02.jar;%APP_HOME%\lib\google-http-client-apache-v2-1.38.0.jar;%APP_HOME%\lib\httpmime-4.5.11.jar;%APP_HOME%\lib\google-oauth-client-1.31.2.jar;%APP_HOME%\lib\google-http-client-jackson2-1.38.0.jar;%APP_HOME%\lib\google-http-client-1.38.0.jar;%APP_HOME%\lib\httpclient-4.5.13.jar;%APP_HOME%\lib\json-20210307.jar;%APP_HOME%\lib\javafx-fxml-11.0.2-win.jar;%APP_HOME%\lib\javafx-web-11.0.2-win.jar;%APP_HOME%\lib\javafx-media-11.0.2-win.jar;%APP_HOME%\lib\javafx-media-11.0.2.jar;%APP_HOME%\lib\javafx-controls-11.0.2-win.jar;%APP_HOME%\lib\javafx-controls-11.0.2.jar;%APP_HOME%\lib\javafx-graphics-11.0.2-win.jar;%APP_HOME%\lib\javafx-graphics-11.0.2.jar;%APP_HOME%\lib\javafx-base-11.0.2-win.jar;%APP_HOME%\lib\javafx-base-11.0.2.jar;%APP_HOME%\lib\microsoft-kiota-serialization-json-1.0.0.jar;%APP_HOME%\lib\gson-2.10.1.jar;%APP_HOME%\lib\commons-beanutils-1.9.2.jar;%APP_HOME%\lib\commons-digester-1.8.1.jar;%APP_HOME%\lib\httpasyncclient-4.1.4.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\commons-collections-3.2.2.jar;%APP_HOME%\lib\microsoft-kiota-http-okHttp-1.0.0.jar;%APP_HOME%\lib\okhttp-4.12.0.jar;%APP_HOME%\lib\okio-jvm-3.6.0.jar;%APP_HOME%\lib\kotlin-stdlib-jdk8-1.9.10.jar;%APP_HOME%\lib\kotlin-stdlib-jdk7-1.9.10.jar;%APP_HOME%\lib\kotlin-stdlib-1.9.10.jar;%APP_HOME%\lib\typetools-0.6.1.jar;%APP_HOME%\lib\doc-annotations-0.2.jar;%APP_HOME%\lib\jackson-annotations-2.13.5.jar;%APP_HOME%\lib\jackson-datatype-jsr310-2.13.5.jar;%APP_HOME%\lib\jackson-databind-2.13.5.jar;%APP_HOME%\lib\jackson-core-2.13.5.jar;%APP_HOME%\lib\snakeyaml-1.23-android.jar;%APP_HOME%\lib\generex-1.0.2.jar;%APP_HOME%\lib\reactor-netty-http-1.0.39.jar;%APP_HOME%\lib\reactor-netty-core-1.0.39.jar;%APP_HOME%\lib\reactor-core-3.4.34.jar;%APP_HOME%\lib\reactive-streams-1.0.4.jar;%APP_HOME%\lib\opencensus-contrib-http-util-0.24.0.jar;%APP_HOME%\lib\guava-30.1-jre.jar;%APP_HOME%\lib\microsoft-kiota-serialization-text-1.0.0.jar;%APP_HOME%\lib\microsoft-kiota-serialization-form-1.0.0.jar;%APP_HOME%\lib\microsoft-kiota-serialization-multipart-1.0.0.jar;%APP_HOME%\lib\microsoft-kiota-abstractions-1.0.0.jar;%APP_HOME%\lib\jakarta.annotation-api-2.1.1.jar;%APP_HOME%\lib\httpcore-nio-4.4.13.jar;%APP_HOME%\lib\httpcore-4.4.13.jar;%APP_HOME%\lib\commons-codec-1.11.jar;%APP_HOME%\lib\kotlin-stdlib-common-1.9.10.jar;%APP_HOME%\lib\annotations-13.0.jar;%APP_HOME%\lib\automaton-1.11-8.jar;%APP_HOME%\lib\failureaccess-1.0.1.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\checker-qual-3.5.0.jar;%APP_HOME%\lib\error_prone_annotations-2.3.4.jar;%APP_HOME%\lib\j2objc-annotations-1.3.jar;%APP_HOME%\lib\azure-json-1.1.0.jar;%APP_HOME%\lib\opentelemetry-api-1.34.1.jar;%APP_HOME%\lib\opentelemetry-context-1.34.1.jar;%APP_HOME%\lib\opentelemetry-semconv-1.23.1-alpha.jar;%APP_HOME%\lib\std-uritemplate-0.0.50.jar;%APP_HOME%\lib\oauth2-oidc-sdk-10.7.1.jar;%APP_HOME%\lib\json-smart-2.4.10.jar;%APP_HOME%\lib\netty-handler-proxy-4.1.101.Final.jar;%APP_HOME%\lib\netty-codec-http2-4.1.101.Final.jar;%APP_HOME%\lib\netty-codec-http-4.1.101.Final.jar;%APP_HOME%\lib\netty-resolver-dns-native-macos-4.1.101.Final-osx-x86_64.jar;%APP_HOME%\lib\netty-resolver-dns-classes-macos-4.1.101.Final.jar;%APP_HOME%\lib\netty-resolver-dns-4.1.101.Final.jar;%APP_HOME%\lib\netty-handler-4.1.101.Final.jar;%APP_HOME%\lib\netty-codec-socks-4.1.101.Final.jar;%APP_HOME%\lib\netty-codec-dns-4.1.101.Final.jar;%APP_HOME%\lib\netty-codec-4.1.101.Final.jar;%APP_HOME%\lib\netty-transport-native-epoll-4.1.101.Final-linux-x86_64.jar;%APP_HOME%\lib\netty-transport-native-kqueue-4.1.101.Final-osx-x86_64.jar;%APP_HOME%\lib\netty-transport-classes-epoll-4.1.101.Final.jar;%APP_HOME%\lib\netty-transport-classes-kqueue-4.1.101.Final.jar;%APP_HOME%\lib\netty-transport-native-unix-common-4.1.101.Final.jar;%APP_HOME%\lib\netty-transport-4.1.101.Final.jar;%APP_HOME%\lib\netty-buffer-4.1.101.Final.jar;%APP_HOME%\lib\netty-tcnative-boringssl-static-2.0.62.Final.jar;%APP_HOME%\lib\netty-resolver-4.1.101.Final.jar;%APP_HOME%\lib\netty-common-4.1.101.Final.jar;%APP_HOME%\lib\nimbus-jose-jwt-9.30.2.jar;%APP_HOME%\lib\jcip-annotations-1.0-1.jar;%APP_HOME%\lib\content-type-2.2.jar;%APP_HOME%\lib\lang-tag-1.7.jar;%APP_HOME%\lib\accessors-smart-2.4.9.jar;%APP_HOME%\lib\netty-tcnative-classes-2.0.62.Final.jar;%APP_HOME%\lib\opencensus-api-0.24.0.jar;%APP_HOME%\lib\asm-9.3.jar;%APP_HOME%\lib\grpc-context-1.22.1.jar


@rem Execute WorkShiftly
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %WORK_SHIFTLY_OPTS%  -classpath "%CLASSPATH%" com.workshiftly.application.ApplicationStarter %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable WORK_SHIFTLY_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%WORK_SHIFTLY_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
