FROM maven:3.9.9-eclipse-temurin-21 AS build

ARG SERVICE_NAME
WORKDIR /workspace

COPY common-lib/pom.xml common-lib/pom.xml
COPY ${SERVICE_NAME}/pom.xml ${SERVICE_NAME}/pom.xml

RUN mvn -f common-lib/pom.xml dependency:go-offline -B || true
RUN mvn -f ${SERVICE_NAME}/pom.xml dependency:go-offline -B || true

COPY common-lib common-lib
COPY ${SERVICE_NAME} ${SERVICE_NAME}

RUN mvn -f common-lib/pom.xml clean install -DskipTests -B
RUN mvn -f ${SERVICE_NAME}/pom.xml clean package -DskipTests -B

FROM eclipse-temurin:21-jre

ARG SERVICE_NAME
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/${SERVICE_NAME}/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
