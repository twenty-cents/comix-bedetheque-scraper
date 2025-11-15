FROM eclipse-temurin:21.0.6_7-jre-alpine-3.21

LABEL org.opencontainers.image.title="comix-bedetheque-scraper" \
      org.opencontainers.image.description="Comix - Micro-service - Bedetheque Scraper" \
      org.opencontainers.image.source="https://github.com/twenty-cents/comix-bedetheque-scraper.git" \
      owner="twenty-cents"

RUN apk add --no-cache curl

ARG JAR_FILE=target/*.jar
WORKDIR /opt/app

COPY ${JAR_FILE} /opt/app/app.jar

ENTRYPOINT ["java","-jar","/opt/app/app.jar"] \
