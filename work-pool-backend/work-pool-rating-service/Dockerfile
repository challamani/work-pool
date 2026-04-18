FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY checkstyle.xml .
COPY work-pool-common ./work-pool-common
COPY work-pool-rating-service ./work-pool-rating-service
RUN mvn --no-transfer-progress -pl work-pool-common,work-pool-rating-service -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/work-pool-rating-service/target/work-pool-rating-service-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
