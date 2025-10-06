@echo off

mvn -pl common,auth-service,eureka-server,gateway,lib-service,user-service -am clean package -DskipTests

pause
