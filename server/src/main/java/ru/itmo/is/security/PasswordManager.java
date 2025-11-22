package ru.itmo.is.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordManager {
    private PasswordManager() {}

    public static boolean matches(String password, String hash) {
        return hash.equals(hash(password));
    }

    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-384");
            byte[] messageDigest = md.digest(password.getBytes());

            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder hash = new StringBuilder(no.toString(16));
            while (hash.length() < 32) {
                hash.insert(0, "0");
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
