package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.MostOrderedProductResponse;
import com.baskiliisler.backend.dto.MostQuotedBrandResponse;
import com.baskiliisler.backend.dto.DealerOverviewResponse;
import com.baskiliisler.backend.dto.ExpiringOffersResponse;
import com.baskiliisler.backend.dto.WeeklyOffersResponse;
import com.baskiliisler.backend.dto.OffersOverviewResponse;
import com.baskiliisler.backend.service.ProductService;
import com.baskiliisler.backend.service.BrandService;
import com.baskiliisler.backend.service.DealerService;
import com.baskiliisler.backend.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Tag(name = "📊 Statistics", description = "İstatistik API'leri")
public class StatisticsController {

    private final ProductService productService;
    private final BrandService brandService;
    private final DealerService dealerService;
    private final QuoteService quoteService;

    @GetMapping("/products/most-ordered")
    @Operation(
        summary = "En çok sipariş alan ürünü getir", 
        description = "Belirtilen dealer'ın veya tüm sistemdeki en çok sipariş alan ürünün bilgilerini döndürür"
    )
    public ResponseEntity<MostOrderedProductResponse> getMostOrderedProduct(
        @RequestParam(required = false) Long dealerId
    ) {
        MostOrderedProductResponse response = productService.getMostOrderedProduct(dealerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/brands/most-quoted")
    @Operation(
        summary = "En çok teklif edilen markayı getir",
        description = "Belirtilen dealer'ın veya tüm sistemdeki en çok teklif edilen markanın bilgilerini döndürür"
    )
    public ResponseEntity<MostQuotedBrandResponse> getMostQuotedBrand(
        @RequestParam(required = false) Long dealerId
    ) {
        MostQuotedBrandResponse response = brandService.getMostQuotedBrand(dealerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dealers/{dealerId}/overview")
    @Operation(
        summary = "Belirli bir dealer'ın genel istatistiklerini getir",
        description = "Belirtilen dealer'ın toplam ürün, marka, teklif ve bekleyen fabrika ataması sayılarını döndürür."
    )
    public ResponseEntity<DealerOverviewResponse> getDealerOverview(@PathVariable Long dealerId) {
        DealerOverviewResponse response = dealerService.getDealerOverview(dealerId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/offers/expiring")
    @Operation(
        summary = "Geçerlilik süresi yaklaşmış teklifleri getir",
        description = "7 gün içinde geçecek tekliflerin listesini ve toplam değerini döndürür."
    )
    public ResponseEntity<ExpiringOffersResponse> getExpiringOffers(@RequestParam(required = false) Long dealerId) {
        ExpiringOffersResponse response = quoteService.getExpiringOffers(dealerId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/offers/weekly")
    @Operation(
        summary = "Haftalık teklif istatistiklerini getir",
        description = "Son 7 günün teklif istatistiklerini döndürür."
    )
    public ResponseEntity<WeeklyOffersResponse> getWeeklyOffers(@RequestParam(required = false) Long dealerId) {
        WeeklyOffersResponse response = quoteService.getWeeklyOffers(dealerId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/offers/overview")
    @Operation(
        summary = "Genel teklif istatistiklerini getir",
        description = "Tüm tekliflerin genel istatistiklerini döndürür."
    )
    public ResponseEntity<OffersOverviewResponse> getOffersOverview(@RequestParam(required = false) Long dealerId) {
        OffersOverviewResponse response = quoteService.getOffersOverview(dealerId);
        return ResponseEntity.ok(response);
    }
} 