package com.zzw.chatserver.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class LocalFileUtilTest {

    @TempDir
    Path uploadDir;

    @Test
    void downloadReadsFileFromLocalUploadUrl() throws Exception {
        Path file = uploadDir.resolve("files").resolve("document.txt");
        Files.createDirectories(file.getParent());
        Files.write(file, "content".getBytes(StandardCharsets.UTF_8));

        byte[] result = LocalFileUtil.download(
                "http://139.196.160.239:18081/chat/uploads/files/document.txt",
                uploadDir.toString()
        );

        assertArrayEquals("content".getBytes(StandardCharsets.UTF_8), result);
    }
}
