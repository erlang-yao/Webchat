package com.zzw.chatserver.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class LocalFileUtil {

    private LocalFileUtil() {
    }

    public static String upload(MultipartFile file, String uploadDir, String baseUrl) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            originalName = "file";
        }

        String ext = "";
        int dotIndex = originalName.lastIndexOf(".");
        if (dotIndex > -1) {
            ext = originalName.substring(dotIndex);
        }

        String subDir = resolveSubDir(ext);
        Path dir = Paths.get(uploadDir, subDir);
        Files.createDirectories(dir);

        String savedName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = dir.resolve(savedName);
        file.transferTo(target.toFile());

        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalizedBaseUrl + "/uploads/" + subDir + "/" + savedName;
    }

    public static String resolveUploadDir(String uploadDir) {
        Path path = Paths.get(uploadDir);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir"), uploadDir);
        }
        return path.toString();
    }

    private static String resolveSubDir(String ext) {
        String extLower = ext.toLowerCase();
        if (extLower.matches("\\.(jpg|jpeg|png|gif|webp|bmp)")) {
            return "image";
        }
        if (extLower.matches("\\.(webm|mp4|mp3|wav|ogg|m4a|aac)")) {
            return "voice";
        }
        return "files";
    }
}
