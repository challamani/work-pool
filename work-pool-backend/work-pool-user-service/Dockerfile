FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY checkstyle.xml .
COPY work-pool-common ./work-pool-common
COPY work-pool-user-service ./work-pool-user-service
RUN mvn --no-transfer-progress -pl work-pool-common,work-pool-user-service -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/work-pool-user-service/target/work-pool-user-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
