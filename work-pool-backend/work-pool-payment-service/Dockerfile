FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY checkstyle.xml .
COPY work-pool-common ./work-pool-common
COPY work-pool-payment-service ./work-pool-payment-service
RUN mvn --no-transfer-progress -pl work-pool-common,work-pool-payment-service -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/work-pool-payment-service/target/work-pool-payment-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
