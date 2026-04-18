FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY checkstyle.xml .
COPY work-pool-common ./work-pool-common
COPY work-pool-notification-service ./work-pool-notification-service
RUN mvn --no-transfer-progress -pl work-pool-common,work-pool-notification-service -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/work-pool-notification-service/target/work-pool-notification-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
