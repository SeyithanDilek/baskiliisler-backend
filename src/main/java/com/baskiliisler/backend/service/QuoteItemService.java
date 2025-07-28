package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.QuoteCreateDto;
import com.baskiliisler.backend.dto.QuoteItemRequestDto;
import com.baskiliisler.backend.model.Product;
import com.baskiliisler.backend.model.Quote;
import com.baskiliisler.backend.model.QuoteItem;
import com.baskiliisler.backend.repository.ProductRepository;
import com.baskiliisler.backend.repository.QuoteItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteItemService {
    private final QuoteItemRepository quoteItemRepository;
    private final ProductRepository productRepository;

    public BigDecimal assembleAndSaveQuoteItems(Quote quote,
                                                 List<QuoteItemRequestDto> quoteItemRequestDtos){
        BigDecimal total = BigDecimal.ZERO;
        for (QuoteItemRequestDto ri : quoteItemRequestDtos) {
            Product p = productRepository.findById(ri.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Ürün yok"));
            
            // KDV hesaplamaları
            BigDecimal lineTotal = ri.unitPrice().multiply(BigDecimal.valueOf(ri.quantity()));
            BigDecimal taxRate = ri.taxRate() != null ? ri.taxRate() : BigDecimal.valueOf(18.00);
            BigDecimal taxAmount = lineTotal.multiply(taxRate.divide(BigDecimal.valueOf(100)));
            BigDecimal lineTotalWithTax = lineTotal.add(taxAmount);
            
            total = total.add(lineTotalWithTax);

            quoteItemRepository.save(QuoteItem.builder()
                    .quote(quote)
                    .product(p)
                    .quantity(ri.quantity())
                    .unitPrice(ri.unitPrice())
                    .taxRate(taxRate)
                    .lineTotal(lineTotal)
                    .taxAmount(taxAmount)
                    .lineTotalWithTax(lineTotalWithTax)
                    .build());
        }
        return total;
    }

    public void deleteQuoteItems(List<QuoteItem> quoteItem){
        quoteItemRepository.deleteAll(quoteItem);
    }
}
