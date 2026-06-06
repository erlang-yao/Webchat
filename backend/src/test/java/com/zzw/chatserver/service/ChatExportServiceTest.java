package com.zzw.chatserver.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void buildInlineDataUriPrefersImageAndVoice() throws Exception {
        ChatExportService service = new ChatExportService();

        Object imageMsg = newMsgInfo("img", "avatar.png");
        Object voiceMsg = newMsgInfo("voice", "voice.webm");

        Method method = ChatExportService.class.getDeclaredMethod("buildInlineDataUri",
                Class.forName("com.zzw.chatserver.service.ChatExportService$MsgInfo"), byte[].class);
        method.setAccessible(true);

        String imageDataUri = (String) method.invoke(service, imageMsg, "img-bytes".getBytes(StandardCharsets.UTF_8));
        String voiceDataUri = (String) method.invoke(service, voiceMsg, "voice-bytes".getBytes(StandardCharsets.UTF_8));

        assertTrue(imageDataUri.startsWith("data:image/png;base64,"));
        assertTrue(voiceDataUri.startsWith("data:audio/webm;base64,"));
    }

    private byte[] invokeDownloadMedia(ChatExportService service, String value) throws Exception {
        Method method = ChatExportService.class.getDeclaredMethod("downloadMedia", String.class);
        method.setAccessible(true);
        return (byte[]) method.invoke(service, value);
    }

    private Object newMsgInfo(String type, String fileRawName) throws Exception {
        Class<?> msgClass = Class.forName("com.zzw.chatserver.service.ChatExportService$MsgInfo");
        Constructor<?> constructor = msgClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object msg = constructor.newInstance();

        ReflectionTestUtils.setField(msg, "messageType", type);
        ReflectionTestUtils.setField(msg, "fileRawName", fileRawName);
        ReflectionTestUtils.setField(msg, "message", Arrays.asList(type, fileRawName).toString());
        return msg;
    }
}
