package com.comix.scrapers.bedetheque.config;

import com.comix.scrapers.bedetheque.exception.NfsVolumeInitializationException;
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
    private static final int MAX_RETRIES = 12;
    private static final long RETRY_DELAY_SECONDS = 5;

    @Override
    public void run(ApplicationArguments args) throws IOException, InterruptedException, NfsVolumeInitializationException {
        log.info("Profil 'int' détecté. Initialisation du volume NFS...");

        waitForNfsServer();
        mountNfsShare();
        createMediaDirectories();
        setPermissions();

        log.info("Volume NFS initialisé et monté avec succès sur {}.", MOUNT_POINT);
    }

    private void waitForNfsServer() throws IOException, InterruptedException {
        log.info("En attente du serveur NFS sur l'hôte '{}'...", NFS_SERVER_HOST);
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            // La commande `showmount` est l'équivalent de la boucle `until` du script
            int exitCode = executeCommand("showmount", "-e", NFS_SERVER_HOST);
            if (exitCode == 0) {
                log.info("Serveur NFS détecté !");
                return;
            }
            if (attempt < MAX_RETRIES) {
                log.warn("Serveur NFS non disponible (tentative {}/{}), nouvelle tentative dans {} secondes...", attempt, MAX_RETRIES, RETRY_DELAY_SECONDS);
                TimeUnit.SECONDS.sleep(RETRY_DELAY_SECONDS);
            }
        }
        throw new NfsVolumeInitializationException("Le serveur NFS n'est pas devenu disponible après " + MAX_RETRIES + " tentatives.");
    }

    private void mountNfsShare() throws IOException, InterruptedException, NfsVolumeInitializationException {
        // Création du point de montage local s'il n'existe pas
        if (!Files.exists(MOUNT_POINT)) {
            log.info("Création du point de montage local : {}", MOUNT_POINT);
            try {
                Files.createDirectories(MOUNT_POINT);
            } catch (IOException e) {
                throw new NfsVolumeInitializationException("Impossible de créer le point de montage local " + MOUNT_POINT, e);
            }
        }

        log.info("Montage du partage NFS v4 de '{}' sur '{}'...", NFS_SERVER_HOST, MOUNT_POINT);
        // Pour NFSv4 avec fsid=0, on monte la racine du pseudo-système de fichiers
        int exitCode = executeCommand("mount", "-t", "nfs", "-o", "nfsvers=4,rw", NFS_SERVER_HOST + ":/", MOUNT_POINT.toString());

        if (exitCode != 0) {
            log.error("Échec du montage du volume NFS (code {}). L'application va s'arrêter.", exitCode);
            throw new NfsVolumeInitializationException("Impossible de monter le volume NFS. Code de sortie: " + exitCode);
        }
        log.info("Partage NFS monté avec succès.");
    }

    private void createMediaDirectories() throws NfsVolumeInitializationException {
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
                throw new NfsVolumeInitializationException("Impossible de créer le répertoire " + subpath, e);
            }
        });
    }

    private void setPermissions() throws IOException {
        log.info("Attribution des permissions (777) sur {}", MOUNT_POINT);
        // Ceci est l'équivalent de `chmod -R 777`. C'est une approche simplifiée.
        // Attention : ne pas utiliser en production sans une stratégie de sécurité adéquate.
        try (Stream<Path> stream = Files.walk(MOUNT_POINT)) {
            stream.forEach(path -> {
                try {
                    boolean readable = path.toFile().setReadable(true, false);
                    boolean writable = path.toFile().setWritable(true, false);
                    boolean executable = path.toFile().setExecutable(true, false);
                    if(!readable || !writable || !executable) {
                        log.warn("Impossible de changer les permissions pour {}", path);
                    }
                } catch (Exception e) {
                    log.warn("Impossible de changer les permissions pour {}", path, e);
                }
            });
        }
    }

    int executeCommand(String... command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command)
                .inheritIO(); // Redirige la sortie/erreur standard du processus vers celle du processus Java
        Process process = processBuilder.start();
        return process.waitFor();
    }
}