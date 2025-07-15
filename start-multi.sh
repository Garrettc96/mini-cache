#!/bin/bash

# Start three instances of the Spring Boot app on ports 8080, 8081, and 8082 using Maven
# Each instance logs to its own file

PEERS="localhost:8080,localhost:8081,localhost:8082"

for PORT in 8080 8081 8082; do
  echo "Starting instance on port $PORT..."
  mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=$PORT --cache.peers=$PEERS" > "app-$PORT.log" 2>&1 &
done

echo "All instances started. Logs: app-8080.log, app-8081.log, app-8082.log" 