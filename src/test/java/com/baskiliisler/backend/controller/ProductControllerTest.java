package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.config.GlobalExceptionHandler;
import com.baskiliisler.backend.dto.ProductRequestDto;
import com.baskiliisler.backend.dto.ProductResponseDto;
import com.baskiliisler.backend.dto.ProductUpdateDto;
import com.baskiliisler.backend.model.Product;
import com.baskiliisler.backend.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController")
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Product testProduct;
    private ProductRequestDto testProductRequestDto;
    private ProductResponseDto testProductResponseDto;
    private ProductUpdateDto testProductUpdateDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        
        testProduct = Product.builder()
                .id(1L)
                .code("PAP_CUP_M")
                .name("Orta Karton Bardak")
                .unit("adet")
                .unitPrice(new BigDecimal("2.50"))
                .active(true)
                .build();

        testProductRequestDto = new ProductRequestDto(
                "PAP_CUP_M",
                "Orta Karton Bardak",
                "adet",
                new BigDecimal("2.50")
        );

        testProductResponseDto = new ProductResponseDto(
                1L,
                "PAP_CUP_M",
                "Orta Karton Bardak",
                "adet",
                new BigDecimal("2.50"),
                true
        );

        testProductUpdateDto = new ProductUpdateDto(
                "PAP_CUP_L",
                "Büyük Karton Bardak",
                "adet",
                new BigDecimal("3.50"),
                true
        );
    }

    @Nested
    @DisplayName("POST /products - Ürün oluşturma")
    class CreateProduct {

        @Test
        @DisplayName("Geçerli verilerle ürün oluşturulduğunda 201 Created döndürmeli")
        void givenValidProductData_whenCreateProduct_thenShouldReturn201() throws Exception {
            // Given
            when(productService.createProduct(any(ProductRequestDto.class))).thenReturn(testProduct);

            // When & Then
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testProductRequestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(testProduct.getId()))
                    .andExpect(jsonPath("$.code").value(testProduct.getCode()))
                    .andExpect(jsonPath("$.name").value(testProduct.getName()))
                    .andExpect(jsonPath("$.unit").value(testProduct.getUnit()))
                    .andExpect(jsonPath("$.unitPrice").value(2.5))
                    .andExpect(jsonPath("$.active").value(testProduct.isActive()));

            verify(productService).createProduct(any(ProductRequestDto.class));
        }

        @Test
        @DisplayName("Geçersiz verilerle ürün oluşturulmaya çalışıldığında 400 Bad Request döndürmeli")
        void givenInvalidProductData_whenCreateProduct_thenShouldReturn400() throws Exception {
            // Given
            ProductRequestDto invalidDto = new ProductRequestDto("", "", "", null);

            // When & Then
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(productService, never()).createProduct(any(ProductRequestDto.class));
        }
    }

    @Nested
    @DisplayName("GET /products - Ürün listeleme")
    class ListProducts {

        @Test
        @DisplayName("Tüm ürünler listelendiğinde 200 OK döndürmeli")
        void whenGetAllProducts_thenShouldReturn200() throws Exception {
            // Given
            when(productService.getAllProducts()).thenReturn(List.of(testProduct));

            // When & Then
            mockMvc.perform(get("/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(testProduct.getId()))
                    .andExpect(jsonPath("$[0].code").value(testProduct.getCode()));

            verify(productService).getAllProducts();
        }

        @Test
        @DisplayName("Aktif ürünler listelendiğinde 200 OK döndürmeli")
        void whenGetActiveProducts_thenShouldReturn200() throws Exception {
            // Given
            when(productService.getActiveProducts()).thenReturn(List.of(testProduct));

            // When & Then
            mockMvc.perform(get("/products/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(testProduct.getId()));

            verify(productService).getActiveProducts();
        }
    }

    @Nested
    @DisplayName("GET /products/{id} - Ürün arama")
    class FindProduct {

        @Test
        @DisplayName("Geçerli ID ile ürün arandığında 200 OK döndürmeli")
        void givenValidId_whenFindById_thenShouldReturn200() throws Exception {
            // Given
            when(productService.findById(1L)).thenReturn(testProductResponseDto);

            // When & Then
            mockMvc.perform(get("/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testProductResponseDto.id()))
                    .andExpect(jsonPath("$.code").value(testProductResponseDto.code()));

            verify(productService).findById(1L);
        }

        @Test
        @DisplayName("Geçersiz ID ile ürün arandığında 404 Not Found döndürmeli")
        void givenInvalidId_whenFindById_thenShouldReturn404() throws Exception {
            // Given
            when(productService.findById(999L)).thenThrow(new EntityNotFoundException("Ürün bulunamadı"));

            // When & Then
            mockMvc.perform(get("/products/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Ürün bulunamadı"));

            verify(productService).findById(999L);
        }

        @Test
        @DisplayName("Geçerli kod ile ürün arandığında 200 OK döndürmeli")
        void givenValidCode_whenFindByCode_thenShouldReturn200() throws Exception {
            // Given
            when(productService.findByCode("PAP_CUP_M")).thenReturn(testProductResponseDto);

            // When & Then
            mockMvc.perform(get("/products/code/PAP_CUP_M"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(testProductResponseDto.code()));

            verify(productService).findByCode("PAP_CUP_M");
        }
    }

    @Nested
    @DisplayName("PATCH /products/{id} - Ürün güncelleme")
    class UpdateProduct {

        @Test
        @DisplayName("Geçerli verilerle ürün güncellendiğinde 200 OK döndürmeli")
        void givenValidUpdateData_whenUpdateProduct_thenShouldReturn200() throws Exception {
            // Given
            when(productService.updateProduct(eq(1L), any(ProductUpdateDto.class)))
                    .thenReturn(testProductResponseDto);

            // When & Then
            mockMvc.perform(patch("/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testProductUpdateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testProductResponseDto.id()));

            verify(productService).updateProduct(eq(1L), any(ProductUpdateDto.class));
        }

        @Test
        @DisplayName("Geçersiz ID ile güncelleme yapıldığında 404 Not Found döndürmeli")
        void givenInvalidId_whenUpdateProduct_thenShouldReturn404() throws Exception {
            // Given
            when(productService.updateProduct(eq(999L), any(ProductUpdateDto.class)))
                    .thenThrow(new EntityNotFoundException("Ürün bulunamadı"));

            // When & Then
            mockMvc.perform(patch("/products/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testProductUpdateDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Ürün bulunamadı"));

            verify(productService).updateProduct(eq(999L), any(ProductUpdateDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /products/{id} - Ürün silme")
    class DeleteProduct {

        @Test
        @DisplayName("Mevcut ürün silindiğinde 204 No Content döndürmeli")
        void givenExistingProduct_whenDeleteProduct_thenShouldReturn204() throws Exception {
            // Given
            doNothing().when(productService).deleteProduct(1L);

            // When & Then
            mockMvc.perform(delete("/products/1"))
                    .andExpect(status().isNoContent());

            verify(productService).deleteProduct(1L);
        }

        @Test
        @DisplayName("Mevcut olmayan ürün silinmeye çalışıldığında 404 Not Found döndürmeli")
        void givenNonExistingProduct_whenDeleteProduct_thenShouldReturn404() throws Exception {
            // Given
            doThrow(new EntityNotFoundException("Ürün bulunamadı")).when(productService).deleteProduct(999L);

            // When & Then
            mockMvc.perform(delete("/products/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Ürün bulunamadı"));

            verify(productService).deleteProduct(999L);
        }
    }

    @Nested
    @DisplayName("PATCH /products/{id}/activate|deactivate - Ürün aktiflik durumu")
    class ProductActivation {

        @Test
        @DisplayName("Ürün aktifleştirildiğinde 200 OK döndürmeli")
        void givenExistingProduct_whenActivateProduct_thenShouldReturn200() throws Exception {
            // Given
            doNothing().when(productService).activateProduct(1L);
            when(productService.findById(1L)).thenReturn(testProductResponseDto);

            // When & Then
            mockMvc.perform(patch("/products/1/activate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testProductResponseDto.id()));

            verify(productService).activateProduct(1L);
            verify(productService).findById(1L);
        }

        @Test
        @DisplayName("Ürün pasifleştirildiğinde 200 OK döndürmeli")
        void givenExistingProduct_whenDeactivateProduct_thenShouldReturn200() throws Exception {
            // Given
            doNothing().when(productService).deactivateProduct(1L);
            when(productService.findById(1L)).thenReturn(testProductResponseDto);

            // When & Then
            mockMvc.perform(patch("/products/1/deactivate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testProductResponseDto.id()));

            verify(productService).deactivateProduct(1L);
            verify(productService).findById(1L);
        }

        @Test
        @DisplayName("Mevcut olmayan ürün aktifleştirilmeye çalışıldığında 404 Not Found döndürmeli")
        void givenNonExistingProduct_whenActivateProduct_thenShouldReturn404() throws Exception {
            // Given
            doThrow(new EntityNotFoundException("Ürün bulunamadı")).when(productService).activateProduct(999L);

            // When & Then
            mockMvc.perform(patch("/products/999/activate"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Ürün bulunamadı"));

            verify(productService).activateProduct(999L);
        }
    }
} 