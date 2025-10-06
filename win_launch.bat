@echo off

@REM load variables from .env
for /f "usebackq tokens=1,2 delims==" %%a in (".env") do (
    set "%%a=%%b"
)
echo LIBRARY_DB_PORT=%LIBRARY_DB_PORT%
echo LIBRARY_DB_USERNAME=%LIBRARY_DB_USERNAME%
echo LIBRARY_DB_PASSWORD=%LIBRARY_DB_PASSWORD%


echo Starting eureka-server
start "" java -jar eureka-server\target\eureka-server-0.0.1-SNAPSHOT.jar
timeout /t 10 >nul

echo Starting auth-service
start "" java -jar auth-service\target\auth-service-0.0.1-SNAPSHOT.jar

echo Starting lib-service
start "" java -jar lib-service\target\lib-service-0.0.1-SNAPSHOT.jar

echo Starting user-service
start "" java -jar user-service\target\user-service-0.0.1-SNAPSHOT.jar

echo Starting gateway
start "" java -jar gateway\target\gateway-0.0.1-SNAPSHOT.jar

echo All services started!
pause
