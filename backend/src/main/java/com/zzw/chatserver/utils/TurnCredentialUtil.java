package com.zzw.chatserver.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class TurnCredentialUtil {

    private static final String HMAC_SHA1 = "HmacSHA1";

    private TurnCredentialUtil() {
    }

    public static String createPassword(String sharedSecret, String username) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(new SecretKeySpec(sharedSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA1));
            return Base64.getEncoder().encodeToString(mac.doFinal(username.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create TURN credential", e);
        }
    }
}
