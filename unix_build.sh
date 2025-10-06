#!/bin/bash

mvn -pl common,auth-service,eureka-server,gateway,lib-service,user-service -am clean package -DskipTests

read -p "Press Enter to exit..."
