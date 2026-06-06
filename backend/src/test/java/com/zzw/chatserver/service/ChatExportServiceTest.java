package com.zzw.chatserver.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ChatExportServiceTest {

    @Test
    void downloadMediaResolvesUploadedHttpsUrlFromConfiguredUploadDir() throws Exception {
        Path tempRoot = Files.createTempDirectory("chat-export");
        Path uploadDir = tempRoot.resolve("uploads");
        Path voiceDir = uploadDir.resolve("voice");
        Files.createDirectories(voiceDir);
        Path file = voiceDir.resolve("sample.webm");
        byte[] expected = "voice-bytes".getBytes(StandardCharsets.UTF_8);
        Files.write(file, expected);

        ChatExportService service = new ChatExportService();
        ReflectionTestUtils.setField(service, "uploadDir", uploadDir.toString());
        ReflectionTestUtils.setField(service, "fileBaseUrl", "https://webchat.beer/chat");

        byte[] actual = invokeDownloadMedia(service, "https://webchat.beer/chat/uploads/voice/sample.webm");
        assertArrayEquals(expected, actual);
    }

    @Test
    void downloadMediaReadsAbsoluteLocalPathDirectly() throws Exception {
        Path tempRoot = Files.createTempDirectory("chat-export");
        Path uploadDir = tempRoot.resolve("uploads");
        Path imageDir = uploadDir.resolve("image");
        Files.createDirectories(imageDir);
        Path file = imageDir.resolve("sample.jpg");
        byte[] expected = "image-bytes".getBytes(StandardCharsets.UTF_8);
        Files.write(file, expected);

        ChatExportService service = new ChatExportService();
        ReflectionTestUtils.setField(service, "uploadDir", uploadDir.toString());
        ReflectionTestUtils.setField(service, "fileBaseUrl", "https://webchat.beer/chat");

        byte[] actual = invokeDownloadMedia(service, file.toAbsolutePath().toString());
        assertArrayEquals(expected, actual);
    }

    private byte[] invokeDownloadMedia(ChatExportService service, String value) throws Exception {
        Method method = ChatExportService.class.getDeclaredMethod("downloadMedia", String.class);
        method.setAccessible(true);
        return (byte[]) method.invoke(service, value);
    }
}
