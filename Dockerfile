# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --system --create-home spring
USER spring

COPY --from=build /app/target/order_engine-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
