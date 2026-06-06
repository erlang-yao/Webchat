package com.zzw.chatserver.utils;

import com.zzw.chatserver.common.ConstValueEnum;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatServerUtilTest {

    @Test
    void randomNicknameUsesConfiguredPrefixAndSixCharacterSuffix() {
        String nickname = ChatServerUtil.randomNickname();

        assertTrue(Arrays.stream(ConstValueEnum.nickNameList)
                .anyMatch(prefix -> nickname.matches(
                        Pattern.quote(prefix) + "-[0-9A-F]{6}"
                )));
    }
}
