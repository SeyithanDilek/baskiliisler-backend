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
            BigDecimal line = ri.unitPrice().multiply(
                    BigDecimal.valueOf(ri.quantity()));
            total = total.add(line);

            quoteItemRepository.save(QuoteItem.builder()
                    .quote(quote)
                    .product(p)
                    .quantity(ri.quantity())
                    .unitPrice(ri.unitPrice())
                    .lineTotal(line)
                    .build());
        }
        return total;
    }

    public void deleteQuoteItems(List<QuoteItem> quoteItem){
        quoteItemRepository.deleteAll(quoteItem);
    }
}
