package com.baskiliisler.backend.util;

import java.security.SecureRandom;

public class PasswordGenerator {
    
    private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*";
    
    private static final SecureRandom RANDOM = new SecureRandom();
    
    public static String generatePassword() {
        StringBuilder password = new StringBuilder();
        
        // En az bir büyük harf
        password.append(UPPER_CASE.charAt(RANDOM.nextInt(UPPER_CASE.length())));
        
        // En az bir küçük harf
        password.append(LOWER_CASE.charAt(RANDOM.nextInt(LOWER_CASE.length())));
        
        // En az bir rakam
        password.append(NUMBERS.charAt(RANDOM.nextInt(NUMBERS.length())));
        
        // En az bir özel karakter
        password.append(SPECIAL_CHARS.charAt(RANDOM.nextInt(SPECIAL_CHARS.length())));
        
        // Kalan 8 karakteri rastgele seç
        String allChars = UPPER_CASE + LOWER_CASE + NUMBERS + SPECIAL_CHARS;
        for (int i = 0; i < 8; i++) {
            password.append(allChars.charAt(RANDOM.nextInt(allChars.length())));
        }
        
        // Şifreyi karıştır
        return shuffleString(password.toString());
    }
    
    private static String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
} 