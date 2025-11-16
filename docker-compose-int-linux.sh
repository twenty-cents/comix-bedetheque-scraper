#!/usr/bin/env bash
# Puisque vous utilisez une image NFS qui dépend du noyau, la seule solution robuste est de s'assurer que le noyau de votre machine (celle qui exécute Docker) a bien chargé le module nécessaire.
sudo modprobe nfsd
# Pour rendre ce changement permanent (recommandé, pour ne pas avoir à le refaire après chaque redémarrage), exécutez la commande suivante. Elle va simplement créer un petit fichier de configuration pour que le système charge ce module à chaque démarrage :
echo "nfsd" | sudo tee /etc/modules-load.d/nfsd.conf
docker-compose -f docker-compose-int.yml up -d --force-recreate