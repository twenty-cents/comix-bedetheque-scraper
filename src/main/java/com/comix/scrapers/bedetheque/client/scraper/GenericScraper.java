package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.exception.BedethequeScraperException;
import com.comix.scrapers.bedetheque.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
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

    public static final String HTML_EXTENSION = ".html";

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
        if(Strings.CI.startsWith(outputMediaBaseUrl, "http")) {
            if(!Strings.CS.endsWith(outputMediaBaseUrl, File.separator)) {
                outputMediaBaseUrl += File.separator;
            }
            hashedOutputMediaUrl = outputMediaBaseUrl + hashedDir + File.separator + mediaFilename;
        } else {
            String hashedOutputMediaDirectory = getHashedPath(outputMediaBaseUrl, hashedDir);
            hashedOutputMediaUrl = hashedOutputMediaDirectory + File.separator + mediaFilename;
        }

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

    protected void download(String originalMediaUrl, String hashedOutputMediaPath) throws BedethequeScraperException {
        // Check if the media has been already downloaded
        var f = new File(hashedOutputMediaPath);
        if (!f.exists()) {
            try {
                boolean isCreated = f.createNewFile();
                if (!isCreated) {
                    String message = "Can't create file for an unknown reason : " + hashedOutputMediaPath;
                    log.debug(message);
                    throw new BedethequeScraperException(ErrorCode.AUTOCOMPLETE_SCRAPING_ERROR, message, new Object[]{hashedOutputMediaPath});
                }
            } catch (IOException e) {
                String message = "Can't create the file : " + hashedOutputMediaPath;
                log.debug(message);
                throw new BedethequeScraperException(ErrorCode.AUTOCOMPLETE_SCRAPING_ERROR, message, new Object[]{hashedOutputMediaPath});
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
            String message = "HTML resource not found : " + originalMediaUrl;
            log.debug(message);
            throw new BedethequeScraperException(ErrorCode.MEDIA_RESOURCE_NOT_FOUND, message, e, new Object[]{originalMediaUrl});
        } catch (FileAlreadyExistsException e2) {
            String message = "File already exists : " + originalMediaUrl;
            log.debug(message);
            throw new BedethequeScraperException(ErrorCode.MEDIA_RESOURCE_ALREADY_EXISTS, message, e2, new Object[]{originalMediaUrl});
        } catch (URISyntaxException e) {
            String message = "Failed to read html : " + originalMediaUrl;
            log.debug(message);
            throw new BedethequeScraperException(ErrorCode.MEDIA_SCRAPING_ERROR, message, e, new Object[]{originalMediaUrl});
        } catch (IOException e) {
            String message = String.format("Cannot save media %s on local file : %s", originalMediaUrl, hashedOutputMediaPath);
            log.debug(message);
            throw new BedethequeScraperException(ErrorCode.MEDIA_RESOURCE_SAVE_ERROR,message, e, new Object[]{originalMediaUrl, hashedOutputMediaPath});
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
        } catch (BedethequeScraperException e) {
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
     * @param originalMediaUrl     The url of the media to download.
     * @return The http path where the saved media is accessible by the local http server.
     */
    public String downloadMedia(String outputMediaDirectory, String outputHttpMediaPath, String originalMediaUrl) {
        String[] mediaUrlParts = StringUtils.split(originalMediaUrl, "/");
        String mediaFilename = mediaUrlParts[mediaUrlParts.length - 1];
        String hashedOutputMediaPath = outputMediaDirectory + File.separator + mediaFilename;
        String httpMediaFilename = outputHttpMediaPath + File.separator + mediaFilename;
        // Check if the media has been already downloaded
        var f = new File(hashedOutputMediaPath);
        if (!f.exists()) {
            try {
                boolean isCreated = f.createNewFile();
                if (!isCreated) {
                    String message = "Can't create file : " + hashedOutputMediaPath;
                    throw new BedethequeScraperException(ErrorCode.MEDIA_DOWNLOAD_ERROR, message, new Object[]{hashedOutputMediaPath});
                }
            } catch (IOException e) {
                String message = "Can't create file : " + hashedOutputMediaPath;
                throw new BedethequeScraperException(ErrorCode.MEDIA_DOWNLOAD_ERROR, message, e,new Object[]{hashedOutputMediaPath});
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
            String message = "HTML resource not found : " + originalMediaUrl;
            log.debug(message);
            throw new BedethequeScraperException(ErrorCode.MEDIA_RESOURCE_NOT_FOUND, message, e, new Object[]{originalMediaUrl});
        } catch (FileAlreadyExistsException e2) {
            String message = "File already exists : " + originalMediaUrl;
            log.debug(message);
            throw new BedethequeScraperException(ErrorCode.MEDIA_RESOURCE_ALREADY_EXISTS, message, e2, new Object[]{originalMediaUrl});
        } catch (URISyntaxException e) {
            String message = "Failed to read html : " + originalMediaUrl;
            log.debug(message);
            throw new BedethequeScraperException(ErrorCode.MEDIA_SCRAPING_ERROR, message, e, new Object[]{originalMediaUrl});
        } catch (IOException e) {
            String message = String.format("Cannot save media %s on local file : %s", originalMediaUrl, hashedOutputMediaPath);
            log.debug(message);
            throw new BedethequeScraperException(ErrorCode.MEDIA_RESOURCE_SAVE_ERROR,message, e, new Object[]{originalMediaUrl, hashedOutputMediaPath});
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
            String message = "Failed to create hashed directory: " + outputMediaDirectory;
            throw new BedethequeScraperException(ErrorCode.MEDIA_DIRECTORY_CREATE_ERROR, message, e, new Object[]{outputMediaDirectory});
        } catch (SecurityException e) {
            String message = "Security exception while creating directory: " + outputMediaDirectory;
            throw new BedethequeScraperException(ErrorCode.MEDIA_DIRECTORY_CREATE_ERROR, message, e, new Object[]{outputMediaDirectory});
        }
        return hashedDirPath.toString();
    }

    /**
     * Build the final graphic novel url to scrap at <a href="https://www.bedetheque.com">...</a>
     *
     * @param url  the graphic novels url to scrap at <a href="https://www.bedetheque.com">...</a>
     * @param page the page number to scrap (optional)
     * @return the final graphic novels url to scrap at <a href="https://www.bedetheque.com">...</a>
     */
    public String buildURl(String url, int page) {
        if (page == 1) {
            return url;
        } else if (page > 1 && page < 10000) {
            String withPage = String.format("__%d.html", page - 1);
            return url.replace(HTML_EXTENSION, withPage);
        } else {
            return url.replace(HTML_EXTENSION, "__10000.html");
        }
    }
}
