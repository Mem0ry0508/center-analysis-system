package org.center.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void verifyReturnsTrueForCorrectPassword() {
        String hash = PasswordUtil.hash("correct-horse");
        assertTrue(PasswordUtil.verify("correct-horse", hash));
    }

    @Test
    void verifyReturnsFalseForWrongPassword() {
        String hash = PasswordUtil.hash("correct-horse");
        assertFalse(PasswordUtil.verify("wrong-password", hash));
    }

    @Test
    void hashIsSaltedSoSamePasswordProducesDifferentHashes() {
        String first = PasswordUtil.hash("same-password");
        String second = PasswordUtil.hash("same-password");
        assertNotEquals(first, second);
        assertTrue(PasswordUtil.verify("same-password", first));
        assertTrue(PasswordUtil.verify("same-password", second));
    }

    @Test
    void verifyReturnsFalseForMalformedStoredHash() {
        assertFalse(PasswordUtil.verify("anything", "not-a-valid-hash"));
    }
}
