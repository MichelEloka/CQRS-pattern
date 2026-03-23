FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY write-api/pom.xml write-api/pom.xml
COPY read-api/pom.xml read-api/pom.xml
COPY sync-service/pom.xml sync-service/pom.xml

RUN mvn -q -pl common,write-api,read-api,sync-service -am dependency:go-offline

COPY common/src common/src
COPY write-api/src write-api/src
COPY read-api/src read-api/src
COPY sync-service/src sync-service/src

RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /opt/apps

COPY --from=build /workspace/write-api/target/write-api-1.0.0.jar /opt/apps/write-api.jar
COPY --from=build /workspace/read-api/target/read-api-1.0.0.jar /opt/apps/read-api.jar
COPY --from=build /workspace/sync-service/target/sync-service-1.0.0.jar /opt/apps/sync-service.jar

ENV APP_NAME=write-api

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /opt/apps/${APP_NAME}.jar"]

