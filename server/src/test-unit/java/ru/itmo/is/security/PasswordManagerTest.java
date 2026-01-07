package ru.itmo.is.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordManagerTest {

    @Test
    void testHash_ShouldReturnDifferentHashesForDifferentPasswords() {
        String password1 = "password1";
        String password2 = "password2";
        
        String hash1 = PasswordManager.hash(password1);
        String hash2 = PasswordManager.hash(password2);
        
        assertNotEquals(hash1, hash2, "Different passwords should produce different hashes");
    }

    @Test
    void testMatches_ShouldReturnTrueForMatchingPassword() {
        String password = "testPassword";
        String hash = PasswordManager.hash(password);
        
        assertTrue(PasswordManager.matches(password, hash),
            "matches() should return true for correct password");
    }

    @Test
    void testMatches_ShouldReturnFalseForNonMatchingPassword() {
        String password = "testPassword";
        String wrongPassword = "wrongPassword";
        String hash = PasswordManager.hash(password);
        
        assertFalse(PasswordManager.matches(wrongPassword, hash), 
            "matches() should return false for incorrect password");
    }
}

