package com.zerobase.cms.domain.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Aes256UtilTest {

    @Test
    void encryptAndDecrypt() {
        String encrypt = Aes256Util.encrypt("Hello world");
        assertEquals(Aes256Util.decrypt(encrypt), "Hello world");
    }
}