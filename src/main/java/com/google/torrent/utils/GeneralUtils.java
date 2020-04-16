package com.google.torrent.utils;

import com.google.torrent.HelloAppEngineApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Slf4j
public class GeneralUtils {
    public static String getSize(long size) {
        if (size < 1024) {
            return size + " bytes";
        } else if (1024 <= size && size <= Math.pow(2, 20)) {
            return (size / Math.pow(2, 10)) + " Kb";
        } else if (Math.pow(2, 20) <= size && size <= Math.pow(2, 30)) {
            return (size / Math.pow(2, 20)) + " Mb";
        } else if (Math.pow(2, 30) <= size && size <= Math.pow(2, 40)) {
            return (size / Math.pow(2, 30)) + " Gb";
        } else {
            return "Very big";
        }
    }

    public static ResponseEntity<Resource> createDownloadFromResource(HttpServletRequest request, Resource resource) {
        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.debug("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    public static boolean isAlreadyExists(Long torrentHashCode) {
        // Might be caused for file with torrent name as hash code
        File uploadFolder;

        if (HelloAppEngineApplication.developEnvironment) {
            uploadFolder = new File("D:/PrFiles/IdeaProjects/uploads/");
        } else {
            uploadFolder = new File("/home/" + System.getProperty("user.name") + "/downloads/");
        }

        for (File file : Objects.requireNonNull(uploadFolder.listFiles())) {
            if (file.getName().contains(String.valueOf(torrentHashCode))) {
                return true;
            }
        }
        return false;
    }
}
