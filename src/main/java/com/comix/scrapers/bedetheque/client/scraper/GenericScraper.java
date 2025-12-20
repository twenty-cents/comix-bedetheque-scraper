package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.exception.BusinessException;
import com.comix.scrapers.bedetheque.exception.TechnicalException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class GenericScraper extends Scraper {

    @Value("${application.downloads.localcache.hashed-directory-step:5000}")
    private int hashedDirectoryStep;

    /**
     * Extract the media filename from a given url
     *
     * @param originalMediaUrl the original media url
     * @return the media filename
     */
    protected String getMediaFilename(String originalMediaUrl) {
        String mediaFilename = null;
        if (originalMediaUrl != null) {
            String[] mediaUrlParts = StringUtils.split(originalMediaUrl, "/");
            mediaFilename = mediaUrlParts[mediaUrlParts.length - 1];
        }
        return mediaFilename;
    }

    protected String getHashedOutputMediaPath(String originalMediaUrl, String outputMediaBasePath, String idMedia) {
        String hashedOutputMediaPath;
        String mediaFilename = getMediaFilename(originalMediaUrl);
        String hashedDir = getHashedRelativeDirectory(idMedia);
        String hashedOutputMediaDirectory = getHashedPath(outputMediaBasePath, hashedDir);
        hashedOutputMediaPath = hashedOutputMediaDirectory + File.separator + mediaFilename;
        return hashedOutputMediaPath;
    }

    protected String getHashedOutputMediaUrl(String originalMediaUrl, String outputMediaBaseUrl, String idMedia) {
        String hashedOutputMediaUrl;
        String mediaFilename = getMediaFilename(originalMediaUrl);
        String hashedDir = getHashedRelativeDirectory(idMedia);
        String hashedOutputMediaDirectory = getHashedPath(outputMediaBaseUrl, hashedDir);
        hashedOutputMediaUrl = hashedOutputMediaDirectory + File.separator + mediaFilename;
        return hashedOutputMediaUrl;
    }

    protected long getMediaSize(String mediaPath) {
        long size = 0;
        if (mediaPath == null) {
            return 0;
        }
        try {
            if(Files.exists(Paths.get(mediaPath))) {
                File file = new File(mediaPath);
                size = FileUtils.sizeOf(file);
            }
        } catch (Exception e) {
            log.error("Can't get size of the filename : {}", mediaPath);
        }
        return size;
    }

    protected void download(String originalMediaUrl, String hashedOutputMediaPath) throws TechnicalException {
        // Check if the media has been already downloaded
        var f = new File(hashedOutputMediaPath);
        if (!f.exists()) {
            try {
                boolean isCreated = f.createNewFile();
                if (!isCreated) {
                    log.debug("Can't create file for an unknown reason : {}", hashedOutputMediaPath);
                    throw new TechnicalException("ERR-SCR-002", new Object[]{hashedOutputMediaPath});
                }
            } catch (IOException e) {
                log.debug("Can't create file : {}", hashedOutputMediaPath);
                throw new TechnicalException("ERR-SCR-002", e, new Object[]{hashedOutputMediaPath});
            }
        } else {
            return;
        }

        // --- Définition des permissions ---
        try {
            Set<PosixFilePermission> perms = new HashSet<>();
            // user permission
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            // group permissions
            perms.add(PosixFilePermission.GROUP_READ);
            // others permissions removed
            perms.remove(PosixFilePermission.OTHERS_READ); // Compliant
            Files.setPosixFilePermissions(f.toPath(), perms);
            log.debug("Permissions 777 set on file: {}", hashedOutputMediaPath);
        } catch (UnsupportedOperationException | IOException e) {
            log.warn("Could not set file permissions for {}. This is expected on non-POSIX systems (like Windows).", hashedOutputMediaPath, e);
        }

        // Download the http media
        try (var fos = new FileOutputStream(hashedOutputMediaPath)) {
            var url = new URI(originalMediaUrl).toURL();
            var output = new ByteArrayOutputStream();

            try (var inputStream = url.openStream()) {
                var n = 0;
                var buffer = new byte[1024];
                while (-1 != (n = inputStream.read(buffer))) {
                    output.write(buffer, 0, n);
                }
            }
            output.writeTo(fos);
            log.info("Resource {} saved to {}", originalMediaUrl, hashedOutputMediaPath);
        } catch (FileNotFoundException e) {
            log.debug("HTML resource not found : {}", originalMediaUrl);
            throw new TechnicalException("ERR-SCR-003", e, new Object[]{originalMediaUrl});
        } catch (FileAlreadyExistsException e2) {
            log.debug("File already exists : {}", originalMediaUrl);
            throw new TechnicalException("ERR-SCR-004", e2, new Object[]{originalMediaUrl});
        } catch (URISyntaxException e) {
            log.debug("Failed to read html : {}", originalMediaUrl);
            throw new TechnicalException("ERR-SCR-005", e, new Object[]{originalMediaUrl});
        } catch (IOException e) {
            log.debug("Cannot save media {} on local file : {}", originalMediaUrl, hashedOutputMediaPath);
            throw new TechnicalException("ERR-SCR-006", e, new Object[]{originalMediaUrl, hashedOutputMediaPath});
        }
    }

    /**
     * Download a media from a http source to a local file
     *
     * @param outputMediaDirectory     Output directory where the media will be saved.
     * @param outputHttpMediaPath      Output http path where the saved media will be accessible by the local http server.
     * @param httpMediaUrl             The url of the media to download.
     * @param httpDefaultMediaFilename the default media file to substitute
     * @param idMedia                  The media id
     * @return The http path where the saved media is accessible by the local http server.
     */
    public String downloadMedia(String outputMediaDirectory, String outputHttpMediaPath, String httpMediaUrl,
                                String httpDefaultMediaFilename, String idMedia) {
        String httpMediaFilename = httpDefaultMediaFilename;

        String hashedDir = getHashedRelativeDirectory(idMedia);
        String hashedOutputMediaDirectory = getHashedPath(outputMediaDirectory, hashedDir);
        String hashedOutputHttpMediaPath = outputHttpMediaPath + hashedDir;
        try {
            httpMediaFilename = downloadMedia(hashedOutputMediaDirectory, hashedOutputHttpMediaPath, httpMediaUrl);
        } catch (BusinessException e) {
            log.warn("Silent fail for the media download {} (Business Exception)", httpMediaUrl);
        } catch (TechnicalException e) {
            log.warn("Silent fail for the media download {} (Technical Exception) - outputDir={}, outputPath={},", httpMediaUrl, outputMediaDirectory, outputHttpMediaPath, e);
        }
        log.debug("Pre-cached file from {} to {}", httpMediaUrl, httpMediaFilename);
        return httpMediaFilename;
    }

    /**
     * Download a media from a http source to a local file
     *
     * @param outputMediaDirectory Output directory where the media will be saved.
     * @param outputHttpMediaPath  Output http path where the saved media will be accessible by the local http server.
     * @param httpMediaUrl         The url of the media to download.
     * @return The http path where the saved media is accessible by the local http server.
     */
    public String downloadMedia(String outputMediaDirectory, String outputHttpMediaPath, String httpMediaUrl) {
        String[] mediaUrlParts = StringUtils.split(httpMediaUrl, "/");
        String mediaFilename = mediaUrlParts[mediaUrlParts.length - 1];
        String mediaFilenamePath = outputMediaDirectory + File.separator + mediaFilename;
        String httpMediaFilename = outputHttpMediaPath + File.separator + mediaFilename;
        // Check if the media has been already downloaded
        var f = new File(mediaFilenamePath);
        if (!f.exists()) {
            try {
                boolean isCreated = f.createNewFile();
                if (!isCreated) {
                    log.debug("Can't create file for an unknown reason : {}", mediaFilenamePath);
                    throw new BusinessException("ERR-SCR-002", new Object[]{mediaFilenamePath});
                }
            } catch (IOException e) {
                log.debug("Can't create file : {}", mediaFilenamePath);
                throw new TechnicalException("ERR-SCR-002", e, new Object[]{mediaFilenamePath});
            }
        } else {
            return httpMediaFilename;
        }

        // --- Définition des permissions ---
        try {
            Set<PosixFilePermission> perms = new HashSet<>();
            // user permission
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            // group permissions
            perms.add(PosixFilePermission.GROUP_READ);
            // others permissions removed
            perms.remove(PosixFilePermission.OTHERS_READ); // Compliant
            Files.setPosixFilePermissions(f.toPath(), perms);
            log.debug("Permissions 777 set on file: {}", mediaFilenamePath);
        } catch (UnsupportedOperationException | IOException e) {
            log.warn("Could not set file permissions for {}. This is expected on non-POSIX systems (like Windows).", mediaFilenamePath, e);
        }

        // Download the http media
        try (var fos = new FileOutputStream(mediaFilenamePath)) {
            var url = new URI(httpMediaUrl).toURL();
            var output = new ByteArrayOutputStream();

            try (var inputStream = url.openStream()) {
                var n = 0;
                var buffer = new byte[1024];
                while (-1 != (n = inputStream.read(buffer))) {
                    output.write(buffer, 0, n);
                }
            }
            output.writeTo(fos);
            log.info("Resource {} saved to {}", httpMediaUrl, mediaFilenamePath);
        } catch (FileNotFoundException e) {
            log.debug("HTML resource not found : {}", httpMediaUrl);
            throw new TechnicalException("ERR-SCR-003", e, new Object[]{httpMediaUrl});
        } catch (FileAlreadyExistsException e2) {
            log.debug("File already exists : {}", httpMediaUrl);
            throw new TechnicalException("ERR-SCR-004", e2, new Object[]{httpMediaUrl});
        } catch (URISyntaxException e) {
            log.debug("Failed to read html : {}", httpMediaUrl);
            throw new TechnicalException("ERR-SCR-005", e, new Object[]{httpMediaUrl});
        } catch (IOException e) {
            log.debug("Cannot save media {} on local file : {}", httpMediaUrl, mediaFilenamePath);
            throw new TechnicalException("ERR-SCR-006", e, new Object[]{httpMediaUrl, mediaFilenamePath});
        }
        return httpMediaFilename;
    }

    /**
     * Calculate the subdirectory where the media should be saved.
     *
     * @param idMedia The media id
     * @return the hashed directory where the media will be saved.
     */
    private String getHashedRelativeDirectory(String idMedia) {
        if (idMedia == null) {
            idMedia = "0";
        }
        String hashedDir = idMedia;
        if (StringUtils.isNumeric(idMedia)) {
            int id = Integer.parseInt(idMedia);
            int hashedDirRange = id / hashedDirectoryStep;
            hashedDir = String.valueOf(hashedDirRange);
        }
        return hashedDir;
    }

    /**
     * Check and create the directory where the media should be saved.
     *
     * @param outputMediaDirectory Default output directory where the media will be saved.
     * @param hashedDir            The hashed directory where the media will be saved.
     * @return Output hashed directory where the media will be saved.
     */
    protected String getHashedPath(String outputMediaDirectory, String hashedDir) {
        Path hashedDirPath = Paths.get(outputMediaDirectory, hashedDir);

        // This is an idempotent and thread-safe way to ensure a directory exists.
        // It creates the directory including any necessary but nonexistent parent directories.
        // If the directory already exists, it does nothing.
        try {
            Files.createDirectories(hashedDirPath);
        } catch (IOException e) {
            log.error("Failed to create hashed directory: {}", outputMediaDirectory, e);
            throw new TechnicalException("ERR-SCR-007", e, new Object[]{outputMediaDirectory});
        } catch (SecurityException e) {
            log.error("Security exception while creating directory: {}. Check permissions.", outputMediaDirectory, e);
            throw new TechnicalException("ERR-SCR-009", e, new Object[]{outputMediaDirectory});
        }
        return hashedDirPath.toString();
    }
}
