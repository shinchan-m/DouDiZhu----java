@echo off
setlocal
cd /d "%~dp0"

call mvnw.cmd -q package
if errorlevel 1 exit /b 1

java -cp "target\test-classes;target\doudizhu.jar" CardRulesTest
if errorlevel 1 exit /b 1
java -cp "target\test-classes;target\doudizhu.jar" AIResponseParserTest
if errorlevel 1 exit /b 1
java -cp "target\test-classes;target\doudizhu.jar" CodeUtilTest
if errorlevel 1 exit /b 1
java -cp "target\test-classes;target\doudizhu.jar" UserStoreTest
if errorlevel 1 exit /b 1
java -cp "target\test-classes;target\doudizhu.jar" CountdownSmokeTest
if errorlevel 1 exit /b 1
java -cp "target\test-classes;target\doudizhu.jar" PokerOrderTest
if errorlevel 1 exit /b 1

echo.
echo All tests passed.
pause
