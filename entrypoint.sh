#!/bin/sh

# entrypoint.sh

# Affiche les options Java qui seront utilisées
echo "JAVA_OPTS: ${JAVA_OPTS}"

# Exécute l'application Java en utilisant les options passées dans JAVA_OPTS
# exec permet au processus Java de devenir le processus principal (PID 1) du conteneur
exec java ${JAVA_OPTS} -jar /opt/app/app.jar