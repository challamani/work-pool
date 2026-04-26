FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build
COPY . .
RUN mvn --no-transfer-progress -pl common,api-gateway -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/api-gateway/target/api-gateway-*.jar app.jar
# Port 8080 for plain HTTP; 8443 for HTTPS (activate ssl profile)
EXPOSE 8080 8443
ENTRYPOINT ["java", "-jar", "app.jar"]
