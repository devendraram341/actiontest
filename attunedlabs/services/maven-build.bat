@echo off

ECHO ----------------------------------------------------------------------
ECHO Maven build for leap-DdlUtils-jdbc
ECHO ----------------------------------------------------------------------
cd attunedlabs/services/leap-DdlUtils-jdbc
del attunedlabs/services/leap-DdlUtils-jdbc/*.classpath
del attunedlabs/services/leap-DdlUtils-jdbc/*.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse

ECHO ----------------------------------------------------------------------
ECHO Maven build for leap-MetaModel-jdbc
ECHO ----------------------------------------------------------------------
cd attunedlabs/services/leap-MetaModel-jdbc
del attunedlabs/services/leap-MetaModel-jdbc/*.classpath
del attunedlabs/services/leap-MetaModel-jdbc/*.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse


ECHO ----------------------------------------------------------------------
ECHO Maven build for leap-MetaModel-cassandra
ECHO ----------------------------------------------------------------------
cd attunedlabs/services/leap-MetaModel-cassandra
del attunedlabs/services/leap-MetaModel-cassandra/*.classpath
del attunedlabs/services/leap-MetaModel-cassandra/*.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse


ECHO ----------------------------------------------------------------------
ECHO Maven build for leap-token-generator
ECHO ----------------------------------------------------------------------
cd attunedlabs/services/leap-token-generator
del attunedlabs/services/leap-token-generator/*.classpath
del attunedlabs/services/leap-token-generator/*.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install -DskipTests
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse


ECHO ----------------------------------------------------------------------
ECHO Maven build for leap-framework
ECHO ----------------------------------------------------------------------
cd attunedlabs/services/leap-framework
del attunedlabs/services/leap-framework/*.classpath
del attunedlabs/services/leap-framework/*.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse

ECHO ----------------------------------------------------------------------
ECHO Maven build for leap-core
ECHO ----------------------------------------------------------------------
cd attunedlabs/services/leap-core
del attunedlabs/services/leap-core/*.classpath
del attunedlabs/services/leap-core/*.project
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse


ECHO ----------------------------------------------------------------------
ECHO Maven build for features-installer
ECHO ----------------------------------------------------------------------
cd attunedlabs/services/features-installer
del attunedlabs/services/features-installer/*.classpath
del attunedlabs/services/features-installer/*.project
del attunedlabs/services/features-installer/tm.out.*
del attunedlabs/services/features-installer/tmlog*
del attunedlabs/services/features-installer/*.epoch
rmdir /s /q "%cd%/target"
rmdir /s /q "%cd%/.settings"
call mvn clean install
if not "%ERRORLEVEL%" == "0" set /p id="Terminate batch job (Y/N)?"/b
if "%id%"=="Y" exit /b
call mvn eclipse:clean
call mvn eclipse:eclipse

pause
