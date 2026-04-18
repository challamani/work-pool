FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/work-pool-notification-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
