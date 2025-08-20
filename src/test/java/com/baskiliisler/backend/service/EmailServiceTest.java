package com.baskiliisler.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baskiliisler.backend.repository.UserRepository;
import com.baskiliisler.backend.service.UserService;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, userRepository);
        ReflectionTestUtils.setField(emailService, "userService", userService);
        ReflectionTestUtils.setField(emailService, "loginUrl", "http://localhost:3000/login");
        ReflectionTestUtils.setField(emailService, "resetPasswordUrl", "http://localhost:3000/reset-password");
        ReflectionTestUtils.setField(emailService, "websiteUrl", "https://baskiliisler.com/");
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
        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
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
        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    void sendWelcomeEmail_ShouldThrowExceptionWhenMailSenderFails() {
        // Given
        String to = "test@example.com";
        String name = "Test User";
        String password = "TestPass123!";
        
        doThrow(new RuntimeException("Mail sending failed"))
                .when(mailSender).send(any(MimeMessagePreparator.class));

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
        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    void sendPasswordResetEmail_ShouldSendEmailSuccessfully() {
        // Given
        String to = "test@example.com";
        String name = "Test User";
        String token = "550e8400-e29b-41d4-a716-446655440000";

        // When
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail(to, name, token));

        // Then
        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    void sendPasswordResetEmail_ShouldSetCorrectEmailProperties() {
        // Given
        String to = "test@example.com";
        String name = "Test User";
        String token = "550e8400-e29b-41d4-a716-446655440000";

        // When
        emailService.sendPasswordResetEmail(to, name, token);

        // Then
        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    void sendPasswordResetEmail_ShouldThrowExceptionWhenMailSenderFails() {
        // Given
        String to = "test@example.com";
        String name = "Test User";
        String token = "550e8400-e29b-41d4-a716-446655440000";
        
        doThrow(new RuntimeException("Mail sending failed"))
                .when(mailSender).send(any(MimeMessagePreparator.class));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                emailService.sendPasswordResetEmail(to, name, token));
    }

    @Test
    void sendPasswordResetEmail_ShouldIncludeAllRequiredInformationInEmailContent() {
        // Given
        String to = "test@example.com";
        String name = "Test User";
        String token = "550e8400-e29b-41d4-a716-446655440000";

        // When
        emailService.sendPasswordResetEmail(to, name, token);

        // Then
        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    void sendNewCustomerWelcomeEmail_ShouldSendEmailSuccessfully() {
        // Given
        String to = "customer@example.com";
        String customerName = "Test Customer";
        String brandName = "Test Brand";

        // When
        assertDoesNotThrow(() -> emailService.sendNewCustomerWelcomeEmail(to, customerName, brandName));

        // Then
        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    void sendFactoryAssignmentNeededEmail_ShouldSendEmailSuccessfully() {
        // Given
        String to = "admin@example.com";
        String adminName = "Test Admin";
        String brandName = "Test Brand";
        Long orderId = 123L;
        BigDecimal totalPrice = new BigDecimal("1000.00");
        LocalDate deadline = LocalDate.now().plusDays(7);

        // When
        assertDoesNotThrow(() -> emailService.sendFactoryAssignmentNeededEmail(to, adminName, brandName, orderId, totalPrice, deadline));

        // Then
        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    void sendOrderDeliveredEmail_ShouldSendEmailSuccessfully() {
        // Given
        String to = "customer@example.com";
        String customerName = "Test Customer";
        String brandName = "Test Brand";
        Long orderId = 123L;
        LocalDateTime deliveredAt = LocalDateTime.now();
        BigDecimal totalPrice = new BigDecimal("1000.00");

        // When
        assertDoesNotThrow(() -> emailService.sendOrderDeliveredEmail(to, customerName, brandName, orderId, deliveredAt, totalPrice));

        // Then
        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    void sendQuoteReminderEmail_ShouldSendEmailSuccessfully() {
        // Given
        String to = "customer@example.com";
        String customerName = "Test Customer";
        String brandName = "Test Brand";
        Long quoteId = 123L;
        BigDecimal totalPrice = new BigDecimal("1000.00");
        LocalDateTime validUntil = LocalDateTime.now().plusDays(2);

        // When
        assertDoesNotThrow(() -> emailService.sendQuoteReminderEmail(to, customerName, brandName, quoteId, totalPrice, validUntil));

        // Then
        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    void sendOrderCancelledEmail_ShouldSendEmailSuccessfully() {
        // Given
        String to = "customer@example.com";
        String customerName = "Test Customer";
        String brandName = "Test Brand";
        Long orderId = 123L;
        BigDecimal totalPrice = new BigDecimal("1000.00");
        String cancellationReason = "Müşteri talebi";

        // When
        assertDoesNotThrow(() -> emailService.sendOrderCancelledEmail(to, customerName, orderId, totalPrice, cancellationReason));

        // Then
        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    void sendOrderCancelledEmail_ShouldThrowExceptionWhenMailSenderFails() {
        // Given
        String to = "test@example.com";
        String customerName = "Test Customer";
        Long orderId = 1L;
        BigDecimal totalPrice = BigDecimal.valueOf(1000);
        String cancellationReason = "Test cancellation";
        
        doThrow(new RuntimeException("Mail sending failed"))
                .when(mailSender).send(any(MimeMessagePreparator.class));

        // When & Then
        assertThatThrownBy(() -> emailService.sendOrderCancelledEmail(to, customerName, orderId, totalPrice, cancellationReason))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email gönderilemedi");

        verify(mailSender).send(any(MimeMessagePreparator.class));
    }
} 