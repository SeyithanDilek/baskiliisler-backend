package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {
    
    @NotBlank(message = "İsim alanı boş olamaz")
    private String name;
    
    @NotBlank(message = "Email alanı boş olamaz")
    @Email(message = "Geçerli bir email adresi giriniz")
    private String email;
    
    @NotBlank(message = "Telefon numarası boş olamaz")
    @Pattern(regexp = "^\\+90\\s?5\\d{2}\\s?\\d{3}\\s?\\d{2}\\s?\\d{2}$", 
             message = "Telefon numarası +90 5XX XXX XX XX formatında olmalıdır")
    private String phoneNumber;
} 