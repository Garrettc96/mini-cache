FROM eclipse-temurin:21-jre
COPY target/mini-cache-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]