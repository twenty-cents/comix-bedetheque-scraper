FROM eclipse-temurin:21.0.6_7-jre-alpine-3.21

LABEL org.opencontainers.image.title="comix-bedetheque-scraper" \
      org.opencontainers.image.description="Comix - Micro-service - Bedetheque Scraper" \
      org.opencontainers.image.source="https://github.com/twenty-cents/comix-bedetheque-scraper.git" \
      owner="twenty-cents"

# Installer curl et les outils NFS
RUN apk add --no-cache curl nfs-utils util-linux

ARG JAR_FILE=target/*.jar
WORKDIR /opt/app

COPY ${JAR_FILE} /opt/app/app.jar

 # Copier le script d'entrée et le rendre exécutable
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]
