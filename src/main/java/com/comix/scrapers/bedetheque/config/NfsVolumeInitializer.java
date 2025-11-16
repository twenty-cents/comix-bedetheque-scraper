package com.comix.scrapers.bedetheque.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Configuration
@Profile({"int"})
public class NfsVolumeInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(NfsVolumeInitializer.class);

    private static final String NFS_SERVER_HOST = "nfs-server";
    private static final Path MOUNT_POINT = Paths.get("/mnt/nfs_share");
    private static final Path MEDIA_ROOT_PATH = MOUNT_POINT.resolve("comix/int/medias/bedetheque");

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Profil 'int' détecté. Initialisation du volume NFS...");

        waitForNfsServer();
        mountNfsShare();
        createMediaDirectories();
        setPermissions();

        log.info("Volume NFS initialisé et monté avec succès sur {}.", MOUNT_POINT);
    }

    private void waitForNfsServer() throws IOException, InterruptedException {
        log.info("En attente du serveur NFS sur l'hôte '{}'...", NFS_SERVER_HOST);
        while (true) {
            // La commande `showmount` est l'équivalent de la boucle `until` du script
            int exitCode = executeCommand("showmount", "-e", NFS_SERVER_HOST);
            if (exitCode == 0) {
                log.info("Serveur NFS détecté !");
                return;
            }
            log.warn("Serveur NFS non disponible (code {}), nouvelle tentative dans 5 secondes...", exitCode);
            TimeUnit.SECONDS.sleep(5);
        }
    }

    private void mountNfsShare() throws IOException, InterruptedException {
        // Création du point de montage local s'il n'existe pas
        if (!Files.exists(MOUNT_POINT)) {
            log.info("Création du point de montage local : {}", MOUNT_POINT);
            Files.createDirectories(MOUNT_POINT);
        }

        log.info("Montage du partage NFS v4 de '{}' sur '{}'...", NFS_SERVER_HOST, MOUNT_POINT);
        // Pour NFSv4 avec fsid=0, on monte la racine du pseudo-système de fichiers
        int exitCode = executeCommand("mount", "-t", "nfs", "-o", "nfsvers=4,rw", NFS_SERVER_HOST + ":/", MOUNT_POINT.toString());

        if (exitCode != 0) {
            log.error("Échec du montage du volume NFS (code {}). L'application va s'arrêter.", exitCode);
            // On peut choisir de lancer une exception pour arrêter le démarrage
            throw new RuntimeException("Impossible de monter le volume NFS.");
        }
        log.info("Partage NFS monté avec succès.");
    }

    private void createMediaDirectories() throws IOException {
        log.info("Création de l'arborescence des répertoires média dans {}", MEDIA_ROOT_PATH);
        // Liste de tous les sous-répertoires à créer
        Stream<String> pathsToCreate = Stream.of(
                "authors/thumbs", "authors/hd",
                "graphic-novels/cover-front/thumbs", "graphic-novels/cover-front/hd",
                "graphic-novels/cover-back/thumbs", "graphic-novels/cover-back/hd",
                "graphic-novels/page-example/thumbs", "graphic-novels/page-example/hd",
                "ratings/avatar",
                "series/cover-front/thumbs",
                "series/page-example/thumbs", "series/page-example/hd"
        );

        // On crée chaque répertoire
        pathsToCreate.forEach(subpath -> {
            try {
                Files.createDirectories(MEDIA_ROOT_PATH.resolve(subpath));
            } catch (IOException e) {
                throw new RuntimeException("Impossible de créer le répertoire " + subpath, e);
            }
        });
    }

    private void setPermissions() throws IOException {
        log.info("Attribution des permissions (777) sur {}", MOUNT_POINT);
        // Ceci est l'équivalent de `chmod -R 777`. C'est une approche simplifiée.
        // Attention : ne pas utiliser en production sans une stratégie de sécurité adéquate.
        Files.walk(MOUNT_POINT)
                .forEach(path -> {
                    try {
                        path.toFile().setReadable(true, false);
                        path.toFile().setWritable(true, false);
                        path.toFile().setExecutable(true, false);
                    } catch (Exception e) {
                        log.warn("Impossible de changer les permissions pour {}", path, e);
                    }
                });
    }

    private int executeCommand(String... command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command)
                .inheritIO(); // Redirige la sortie/erreur standard du processus vers celle du processus Java
        Process process = processBuilder.start();
        return process.waitFor();
    }
}