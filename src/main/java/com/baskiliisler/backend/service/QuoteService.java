package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.QuoteCreateDto;
import com.baskiliisler.backend.dto.QuoteUpdateDto;
import com.baskiliisler.backend.model.*;
import com.baskiliisler.backend.repository.*;
import com.baskiliisler.backend.type.ProcessStatus;
import com.baskiliisler.backend.type.QuoteStatus;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final BrandRepository brandRepo;
    private final QuoteRepository quoteRepo;
    private final QuoteItemService quoteItemService;
    private final BrandProcessService brandProcessService;
    private final BrandProcessHistoryService brandProcessHistoryService;
    private final OrderService orderService;

    @Transactional
    public Quote createQuote(QuoteCreateDto quoteCreateDto) {

        Brand brand = brandRepo.findById(quoteCreateDto.brandId())
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));

        Quote quote = quoteRepo.save(Quote.builder()
                .brand(brand)
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(quoteCreateDto.validUntil())
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.ZERO)
                .build());

        BigDecimal totalPrice = quoteItemService.assembleAndSaveQuoteItems(quote, quoteCreateDto.items());
        quote.setTotalPrice(totalPrice);

        BrandProcess brandProcess = brandProcessService.updateBrandProcessStatus(brand.getId(), ProcessStatus.OFFER_SENT);

        brandProcessHistoryService.saveProcessHistoryForChangeStatus(
                brandProcess,
                ProcessStatus.OFFER_SENT,  // toStatus
                ProcessStatus.SAMPLE_LEFT,  // fromStatus
                "{\"quoteId\":" + quote.getId() + "}");

        return quote;
    }

    @Transactional
    public Quote updateQuote(Long quoteId,QuoteUpdateDto quoteUpdateDto) {
        Quote quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Teklif bulunamadı"));

        if (quote.getStatus() != QuoteStatus.DRAFT &&
                quote.getStatus() != QuoteStatus.OFFER_SENT) {
            throw new IllegalStateException("Bu teklif güncellenemez");
        }

        quoteItemService.deleteQuoteItems(quote.getItems());
        quote.getItems().clear();
        BigDecimal total = quoteItemService.assembleAndSaveQuoteItems(quote, quoteUpdateDto.items());

        if (quoteUpdateDto.validUntil() != null) {
            quote.setValidUntil(quoteUpdateDto.validUntil());
        }
        quote.setTotalPrice(total);
        quote.setUpdatedAt(LocalDateTime.now());
        quote.setStatus(QuoteStatus.OFFER_SENT);

        BrandProcess brandProcess = brandProcessService.updateBrandProcessStatus(quote.getBrand().getId(), ProcessStatus.OFFER_SENT);

        brandProcessHistoryService.saveProcessHistoryForChangeStatus(
                brandProcess,
                ProcessStatus.OFFER_SENT,  // toStatus
                ProcessStatus.OFFER_SENT,  // fromStatus
                "Revizyon yapıldı. Teklif ID: " + quote.getId());
        return quote;
    }

    @Transactional
    public Order acceptQuote(Long quoteId,
                             Map<Long, LocalDate> deadlines) {
        Quote quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Teklif bulunamadı"));

        if (quote.getStatus() != QuoteStatus.OFFER_SENT) {
            throw new IllegalStateException("Quote cannot be accepted");
        }

        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setUpdatedAt(LocalDateTime.now());

        Order order = orderService.createOrderFromQuote(quote, deadlines);
        BrandProcess savedBrandProcess = brandProcessService.updateBrandProcessStatus(quote.getBrand().getId(), ProcessStatus.OFFER_SENT);
        brandProcessHistoryService.saveProcessHistoryForChangeStatus(savedBrandProcess,
                ProcessStatus.ORDER_PLACED,  // toStatus
                ProcessStatus.OFFER_SENT,    // fromStatus
                "Teklif kabul edildi. Sipariş ID: " + order.getId());
        return order;
    }

    @Transactional
    public void expireQuote(Long quoteId) {
        Quote quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Teklif bulunamadı"));

        if (quote.getStatus() != QuoteStatus.OFFER_SENT) {
            throw new IllegalStateException("Teklif süresi dolmuş");
        }

        quote.setStatus(QuoteStatus.EXPIRED);
        quote.setUpdatedAt(LocalDateTime.now());

        BrandProcess brandProcess = brandProcessService.checkForExpired(quote.getBrand().getId());

        brandProcessHistoryService.saveProcessHistoryForChangeStatus(
                brandProcess,
                ProcessStatus.EXPIRED,     // toStatus
                ProcessStatus.OFFER_SENT,  // fromStatus  
                "Teklif süresi doldu. Teklif ID: " + quote.getId());
    }
}
