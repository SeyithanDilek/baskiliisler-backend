package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.config.JwtFilter;
import com.baskiliisler.backend.config.JwtUtil;
import com.baskiliisler.backend.config.SecurityConfig;
import com.baskiliisler.backend.config.TestSecurityConfig;
import com.baskiliisler.backend.dto.BrandRequestDto;
import com.baskiliisler.backend.dto.BrandDetailDto;
import com.baskiliisler.backend.dto.BrandUpdateDto;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.repository.UserRepository;
import com.baskiliisler.backend.service.BrandService;
import com.baskiliisler.backend.type.ProcessStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(BrandController.class)
@Import(TestSecurityConfig.class)
class BrandControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private BrandService brandService;

    @InjectMocks
    private BrandController brandController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(brandController)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Yeni marka oluşturulduğunda")
    void whenCreateBrand_thenReturnCreated() throws Exception {
        // given
        BrandRequestDto request = new BrandRequestDto(
                "Test Brand",
                "contact@test.com",
                "1234567890"
        );

        Brand createdBrand = Brand.builder()
                .id(1L)
                .name(request.name())
                .contactEmail(request.contactEmail())
                .contactPhone(request.contactPhone())
                .build();

        when(brandService.createBrand(any(BrandRequestDto.class))).thenReturn(createdBrand);

        // when & then
        mockMvc.perform(post("/brands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(createdBrand.getId()))
                .andExpect(jsonPath("$.name").value(createdBrand.getName()))
                .andExpect(jsonPath("$.contactEmail").value(createdBrand.getContactEmail()))
                .andExpect(jsonPath("$.contactPhone").value(createdBrand.getContactPhone()));
    }

    @Test
    @WithMockUser(roles = "REP")
    @DisplayName("Tüm markalar listelendiğinde")
    void whenGetAllBrands_thenReturnList() throws Exception {
        // given
        Brand brand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .contactEmail("contact@test.com")
                .contactPhone("1234567890")
                .build();

        when(brandService.getAllBrands()).thenReturn(List.of(brand));

        // when & then
        mockMvc.perform(get("/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(brand.getId()))
                .andExpect(jsonPath("$[0].name").value(brand.getName()))
                .andExpect(jsonPath("$[0].contactEmail").value(brand.getContactEmail()))
                .andExpect(jsonPath("$[0].contactPhone").value(brand.getContactPhone()));
    }

    @Test
    @DisplayName("Kimlik doğrulaması olmadan istek yapıldığında")
    void whenUnauthenticatedRequest_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/brands"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ID ile marka detayı bulunduğunda")
    void whenGetById_thenReturnBrandDetail() throws Exception {
        // given
        Long brandId = 1L;
        BrandDetailDto detailDto = new BrandDetailDto(
                brandId,
                "Test Brand",
                "contact@test.com",
                "1234567890",
                ProcessStatus.SAMPLE_LEFT
        );

        when(brandService.findById(brandId)).thenReturn(detailDto);

        // when & then
        mockMvc.perform(get("/brands/{id}", brandId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(detailDto.id()))
                .andExpect(jsonPath("$.name").value(detailDto.name()))
                .andExpect(jsonPath("$.contactEmail").value(detailDto.contactEmail()))
                .andExpect(jsonPath("$.contactPhone").value(detailDto.contactPhone()))
                .andExpect(jsonPath("$.status").value(detailDto.status().name()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Marka güncellendiğinde")
    void whenUpdateBrand_thenReturnUpdatedBrandDetail() throws Exception {
        // given
        Long brandId = 1L;
        BrandUpdateDto updateDto = new BrandUpdateDto(
                "Updated Brand",
                "updated@test.com",
                "9876543210"
        );

        BrandDetailDto updatedDetail = new BrandDetailDto(
                brandId,
                updateDto.name(),
                updateDto.contactEmail(),
                updateDto.contactPhone(),
                ProcessStatus.SAMPLE_LEFT
        );

        when(brandService.updateBrand(eq(brandId), any(BrandUpdateDto.class)))
                .thenReturn(updatedDetail);

        // when & then
        mockMvc.perform(patch("/brands/{id}", brandId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedDetail.id()))
                .andExpect(jsonPath("$.name").value(updatedDetail.name()))
                .andExpect(jsonPath("$.contactEmail").value(updatedDetail.contactEmail()))
                .andExpect(jsonPath("$.contactPhone").value(updatedDetail.contactPhone()))
                .andExpect(jsonPath("$.status").value(updatedDetail.status().name()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Marka silindiğinde")
    void whenDeleteBrand_thenReturnNoContent() throws Exception {
        // given
        Long brandId = 1L;

        // when & then
        mockMvc.perform(delete("/brands/{id}", brandId))
                .andExpect(status().isNoContent());

        verify(brandService).deleteBrand(brandId);
    }
} 