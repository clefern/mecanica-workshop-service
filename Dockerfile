FROM maven:3.9-eclipse-temurin-21 AS build
ARG GITHUB_TOKEN
WORKDIR /app

RUN mkdir -p /root/.m2 && \
    printf '<settings><servers><server><id>github</id><username>x-access-token</username><password>%s</password></server></servers></settings>' \
    "${GITHUB_TOKEN}" > /root/.m2/settings.xml

COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn --batch-mode package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget unzip && \
    mkdir -p /app/newrelic && \
    wget -q -O /tmp/newrelic.zip https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip -j /tmp/newrelic.zip newrelic/newrelic.jar -d /app/newrelic/ && \
    rm /tmp/newrelic.zip && \
    apk del wget unzip
COPY --from=build /app/target/mecanica-workshop-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-javaagent:/app/newrelic/newrelic.jar", "-jar", "app.jar"]
