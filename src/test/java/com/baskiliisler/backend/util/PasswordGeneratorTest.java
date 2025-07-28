package com.baskiliisler.backend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    @Test
    void generatePassword_ShouldReturn12CharacterPassword() {
        // When
        String password = PasswordGenerator.generatePassword();

        // Then
        assertEquals(12, password.length());
    }

    @Test
    void generatePassword_ShouldContainAtLeastOneUpperCase() {
        // When
        String password = PasswordGenerator.generatePassword();

        // Then
        assertTrue(password.matches(".*[A-Z].*"), "Password should contain at least one uppercase letter");
    }

    @Test
    void generatePassword_ShouldContainAtLeastOneLowerCase() {
        // When
        String password = PasswordGenerator.generatePassword();

        // Then
        assertTrue(password.matches(".*[a-z].*"), "Password should contain at least one lowercase letter");
    }

    @Test
    void generatePassword_ShouldContainAtLeastOneDigit() {
        // When
        String password = PasswordGenerator.generatePassword();

        // Then
        assertTrue(password.matches(".*\\d.*"), "Password should contain at least one digit");
    }

    @Test
    void generatePassword_ShouldContainAtLeastOneSpecialChar() {
        // When
        String password = PasswordGenerator.generatePassword();

        // Then
        assertTrue(password.matches(".*[!@#$%^&*].*"), "Password should contain at least one special character");
    }

    @Test
    void generatePassword_ShouldGenerateDifferentPasswords() {
        // When
        String password1 = PasswordGenerator.generatePassword();
        String password2 = PasswordGenerator.generatePassword();

        // Then
        assertNotEquals(password1, password2, "Generated passwords should be different");
    }

    @Test
    void generatePassword_ShouldOnlyContainValidCharacters() {
        // When
        String password = PasswordGenerator.generatePassword();

        // Then
        assertTrue(password.matches("^[A-Za-z0-9!@#$%^&*]+$"), 
                "Password should only contain valid characters: A-Z, a-z, 0-9, !@#$%^&*");
    }
} 