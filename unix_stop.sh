#!/bin/bash

echo "Stopping services..."

for service in eureka-server auth-service lib-service user-service gateway; do
    pid_file="unix/pids/$service.pid"

    if [ -f "$pid_file" ]; then
        pid=$(cat "$pid_file")
        if kill -0 "$pid" > /dev/null 2>&1; then
            kill "$pid"
            echo "Stopped $service (PID $pid)"
        else
            echo "$service (PID $pid) is not running"
        fi
        rm -f "$pid_file"
    else
        echo "PID file for $service not found"
    fi
done

echo "All services stopped."
