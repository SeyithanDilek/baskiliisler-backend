package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.BrandDetailDto;
import com.baskiliisler.backend.dto.BrandRequestDto;
import com.baskiliisler.backend.dto.BrandUpdateDto;
import com.baskiliisler.backend.model.Brand;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BrandControllerTest {

    @Mock
    private BrandService brandService;

    @InjectMocks
    private BrandController brandController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(brandController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
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
    @DisplayName("Tüm markalar listelendiğinde")
    void whenGetAllBrands_thenReturnList() throws Exception {
        // given
        Brand brand1 = Brand.builder()
                .id(1L)
                .name("Brand 1")
                .contactEmail("brand1@test.com")
                .contactPhone("1234567890")
                .build();

        Brand brand2 = Brand.builder()
                .id(2L)
                .name("Brand 2")
                .contactEmail("brand2@test.com")
                .contactPhone("0987654321")
                .build();

        when(brandService.getAllBrands()).thenReturn(List.of(brand1, brand2));

        // when & then
        mockMvc.perform(get("/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(brand1.getId()))
                .andExpect(jsonPath("$[0].name").value(brand1.getName()))
                .andExpect(jsonPath("$[1].id").value(brand2.getId()))
                .andExpect(jsonPath("$[1].name").value(brand2.getName()));
    }

    @Test
    @DisplayName("Marka detayı getirildiğinde")
    void whenGetById_thenReturnBrandDetail() throws Exception {
        // given
        Long brandId = 1L;
        BrandDetailDto brand = new BrandDetailDto(
                brandId,
                "Test Brand",
                "contact@test.com",
                "1234567890",
                ProcessStatus.INIT
        );

        when(brandService.findById(brandId)).thenReturn(brand);

        // when & then
        mockMvc.perform(get("/brands/{id}", brandId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(brand.id()))
                .andExpect(jsonPath("$.name").value(brand.name()))
                .andExpect(jsonPath("$.contactEmail").value(brand.contactEmail()))
                .andExpect(jsonPath("$.contactPhone").value(brand.contactPhone()))
                .andExpect(jsonPath("$.status").value(brand.status().name()));
    }

    @Test
    @DisplayName("Marka güncellendiğinde")
    void whenUpdateBrand_thenReturnUpdatedBrandDetail() throws Exception {
        // given
        Long brandId = 1L;
        BrandUpdateDto request = new BrandUpdateDto(
                "Updated Brand",
                "updated@test.com",
                "9876543210"
        );

        BrandDetailDto updatedBrand = new BrandDetailDto(
                brandId,
                request.name(),
                request.contactEmail(),
                request.contactPhone(),
                ProcessStatus.INIT
        );

        when(brandService.updateBrand(eq(brandId), any(BrandUpdateDto.class))).thenReturn(updatedBrand);

        // when & then
        mockMvc.perform(patch("/brands/{id}", brandId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedBrand.id()))
                .andExpect(jsonPath("$.name").value(updatedBrand.name()))
                .andExpect(jsonPath("$.contactEmail").value(updatedBrand.contactEmail()))
                .andExpect(jsonPath("$.contactPhone").value(updatedBrand.contactPhone()))
                .andExpect(jsonPath("$.status").value(updatedBrand.status().name()));
    }

    @Test
    @DisplayName("Marka silindiğinde")
    void whenDeleteBrand_thenReturnNoContent() throws Exception {
        // given
        Long brandId = 1L;

        // when & then
        mockMvc.perform(delete("/brands/{id}", brandId))
                .andExpect(status().isNoContent());
    }
} 