package com.zzw.chatserver.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ChatExportServiceLegacyPathTest {

    @Test
    void downloadMediaFallsBackFromLegacyAbsoluteUploadPath() throws Exception {
        Path tempRoot = Files.createTempDirectory("chat-export-legacy");
        Path uploadDir = tempRoot.resolve("uploads");
        Path voiceDir = uploadDir.resolve("voice");
        Files.createDirectories(voiceDir);
        Path file = voiceDir.resolve("legacy.webm");
        byte[] expected = "legacy-voice".getBytes(StandardCharsets.UTF_8);
        Files.write(file, expected);

        ChatExportService service = new ChatExportService();
        ReflectionTestUtils.setField(service, "uploadDir", uploadDir.toString());
        ReflectionTestUtils.setField(service, "fileBaseUrl", "https://webchat.beer/chat");

        byte[] actual = invokeDownloadMedia(service, "/www/wwwroot/uploads/voice/legacy.webm");
        assertArrayEquals(expected, actual);
    }

    private byte[] invokeDownloadMedia(ChatExportService service, String value) throws Exception {
        Method method = ChatExportService.class.getDeclaredMethod("downloadMedia", String.class);
        method.setAccessible(true);
        return (byte[]) method.invoke(service, value);
    }
}
