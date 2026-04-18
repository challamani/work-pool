FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY checkstyle.xml .
COPY work-pool-common ./work-pool-common
COPY work-pool-api-gateway ./work-pool-api-gateway
RUN mvn --no-transfer-progress -pl work-pool-common,work-pool-api-gateway -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/work-pool-api-gateway/target/work-pool-api-gateway-*.jar app.jar
# Port 8080 for plain HTTP; 8443 for HTTPS (activate ssl profile)
EXPOSE 8080 8443
ENTRYPOINT ["java", "-jar", "app.jar"]
