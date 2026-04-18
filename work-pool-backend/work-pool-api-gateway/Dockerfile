FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/work-pool-api-gateway-*.jar app.jar
# Port 8080 for plain HTTP; 8443 for HTTPS (activate ssl profile)
EXPOSE 8080 8443
ENTRYPOINT ["java", "-jar", "app.jar"]
