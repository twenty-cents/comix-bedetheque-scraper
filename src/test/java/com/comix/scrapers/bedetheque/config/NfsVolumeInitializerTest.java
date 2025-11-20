package com.comix.scrapers.bedetheque.config;

import com.comix.scrapers.bedetheque.exception.NfsVolumeInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NfsVolumeInitializerTest {

    // On utilise @Spy pour pouvoir mocker la méthode `executeCommand` sur une instance réelle.
    @Spy
    @InjectMocks
    private NfsVolumeInitializer nfsVolumeInitializer;

    @Mock
    private ApplicationArguments applicationArguments;

    // Mock statique pour toutes les méthodes de la classe `Files`.
    private MockedStatic<Files> filesMockedStatic;

    @BeforeEach
    void setUp() {
        // Initialise le mock pour les méthodes statiques de `Files` avant chaque test.
        filesMockedStatic = mockStatic(Files.class);
    }

    @AfterEach
    void tearDown() {
        // Ferme le mock statique après chaque test pour éviter les fuites.
        filesMockedStatic.close();
    }

    @Test
    void run_shouldSucceed_whenAllCommandsAndFileOperationsAreSuccessful() throws IOException, InterruptedException {
        // Given: Tous les prérequis sont remplis
        Path mountPoint = Paths.get("/mnt/nfs_share");

        // Simule la commande `showmount` qui réussit du premier coup.
        doReturn(0).when(nfsVolumeInitializer).executeCommand("showmount", "-e", "nfs-server");

        // Simule la commande `mount` qui réussit.
        doReturn(0).when(nfsVolumeInitializer).executeCommand("mount", "-t", "nfs", "-o", "nfsvers=4,rw", "nfs-server:/", mountPoint.toString());

        // Simule les opérations sur les fichiers
        filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
        filesMockedStatic.when(() -> Files.createDirectories(any(Path.class))).thenReturn(null);
        
        // Simule `Files.walk` pour retourner un stream vide (on ne teste pas la logique interne de `setPermissions`).
        filesMockedStatic.when(() -> Files.walk(any(Path.class))).thenReturn(Stream.empty());

        // When: On exécute la méthode `run`.
        // Then: Aucune exception ne doit être levée.
        assertDoesNotThrow(() -> nfsVolumeInitializer.run(applicationArguments));

        // Verify: On vérifie que les méthodes principales ont été appelées.
        verify(nfsVolumeInitializer, times(1)).executeCommand("showmount", "-e", "nfs-server");
        verify(nfsVolumeInitializer, times(1)).executeCommand(eq("mount"), any(), any(), any(), any(), any(), any());
        filesMockedStatic.verify(() -> Files.walk(mountPoint), times(1));
    }

    @Test
    void run_shouldFail_whenNfsServerIsNotAvailableAfterMaxRetries() throws IOException, InterruptedException {
        // Given: La commande `showmount` échoue constamment.
        doReturn(1).when(nfsVolumeInitializer).executeCommand("showmount", "-e", "nfs-server");

        // When & Then: On s'attend à ce qu'une NfsVolumeInitializationException soit levée.
        assertThrows(NfsVolumeInitializationException.class, () -> nfsVolumeInitializer.run(applicationArguments));

        // Verify: On vérifie que la commande a été tentée 12 fois (MAX_RETRIES).
        verify(nfsVolumeInitializer, times(12)).executeCommand("showmount", "-e", "nfs-server");
    }

    @Test
    void run_shouldFail_whenMountCommandFails() throws IOException, InterruptedException {
        // Given: `showmount` réussit, mais `mount` échoue.
        doReturn(0).when(nfsVolumeInitializer).executeCommand("showmount", "-e", "nfs-server");
        doReturn(1).when(nfsVolumeInitializer).executeCommand(eq("mount"), any(), any(), any(), any(), any(), any());

        // When & Then: On s'attend à une exception.
        assertThrows(NfsVolumeInitializationException.class, () -> nfsVolumeInitializer.run(applicationArguments));

        // Verify: On vérifie que `mount` a bien été appelé.
        verify(nfsVolumeInitializer, times(1)).executeCommand(eq("mount"), any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_shouldFail_whenDirectoryCreationFails() throws IOException, InterruptedException {
        // Given: Les commandes réussissent, mais la création d'un répertoire échoue.
        // On utilise lenient() car l'exception peut être levée avant que tous les mocks ne soient utilisés.
        lenient().doReturn(0).when(nfsVolumeInitializer).executeCommand(eq("showmount"), any(), any());
        lenient().doReturn(0).when(nfsVolumeInitializer).executeCommand(eq("mount"), any(), any(), any(), any(), any(), any());

        // Simule une IOException lors de la création des répertoires.
        // La méthode createMediaDirectories attrape l'IOException et la ré-encapsule dans une NfsVolumeInitializationException.
        // Nous devons donc simuler l'exception que la méthode sous-jacente peut réellement lever.
        filesMockedStatic.when(() -> Files.createDirectories(any(Path.class))).thenThrow(IOException.class);

        // When & Then: On s'attend à une exception.
        assertThrows(NfsVolumeInitializationException.class, () -> nfsVolumeInitializer.run(applicationArguments));
    }

    @Test
    void setPermissions_shouldNotThrow_whenSetPermissionsFailsForOneFile() {
        // Given: On simule un `Files.walk` qui retourne un chemin.
        Path mockPath = mock(Path.class);
        File mockFile = mock(File.class);
        when(mockPath.toFile()).thenReturn(mockFile);

        // Simule une erreur lors du changement de permission.
        when(mockFile.setReadable(anyBoolean(), anyBoolean())).thenThrow(new SecurityException("Access Denied"));

        filesMockedStatic.when(() -> Files.walk(any(Path.class))).thenReturn(Stream.of(mockPath));

        // When & Then: La méthode `setPermissions` ne doit pas propager l'exception (elle doit juste la logger).
        assertDoesNotThrow(() -> {
            // On appelle la méthode privée via la réflexion pour la tester de manière isolée.
            // En pratique, on testerait via la méthode publique `run`.
            var method = NfsVolumeInitializer.class.getDeclaredMethod("setPermissions");
            method.setAccessible(true);
            method.invoke(nfsVolumeInitializer);
        });

        // Verify: On vérifie que `setReadable` a bien été appelé.
        verify(mockFile, times(1)).setReadable(true, false);
    }
}