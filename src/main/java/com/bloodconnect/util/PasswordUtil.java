package com.bloodconnect.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password hashing utility using BCrypt.
 * Never store or compare plain-text passwords.
 */
public class PasswordUtil {

    private static final int LOG_ROUNDS = 10;

    /**
     * Hashes a plain-text password using BCrypt with a random salt.
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Checks a plain-text password against a stored BCrypt hash.
     * Returns true if the password matches.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
