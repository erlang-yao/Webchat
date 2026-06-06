package com.zzw.chatserver.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TurnCredentialUtilTest {

    @Test
    void createsCoturnRestApiPassword() {
        assertEquals(
                "4Mf1qnpiiPUvXdzLsfEdbClBIEQ=",
                TurnCredentialUtil.createPassword("secret", "1700000000:user-1")
        );
    }
}
