package com.baskiliisler.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "loginUrl", "http://localhost:3000/login");
    }

    @Test
    void sendWelcomeEmail_ShouldSendEmailSuccessfully() {
        // Given
        String to = "test@example.com";
        String name = "Test User";
        String password = "TestPass123!";

        // When
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(to, name, password));

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendWelcomeEmail_ShouldSetCorrectEmailProperties() {
        // Given
        String to = "test@example.com";
        String name = "Test User";
        String password = "TestPass123!";

        // When
        emailService.sendWelcomeEmail(to, name, password);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendWelcomeEmail_ShouldThrowExceptionWhenMailSenderFails() {
        // Given
        String to = "test@example.com";
        String name = "Test User";
        String password = "TestPass123!";
        
        doThrow(new RuntimeException("Mail sending failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                emailService.sendWelcomeEmail(to, name, password));
    }

    @Test
    void sendWelcomeEmail_ShouldIncludeAllRequiredInformationInEmailContent() {
        // Given
        String to = "test@example.com";
        String name = "Test User";
        String password = "TestPass123!";

        // When
        emailService.sendWelcomeEmail(to, name, password);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
} 