package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.QuoteCreateDto;
import com.baskiliisler.backend.dto.QuoteResponseDto;
import com.baskiliisler.backend.dto.QuoteUpdateDto;
import com.baskiliisler.backend.mapper.QuoteMapper;
import com.baskiliisler.backend.model.Quote;
import com.baskiliisler.backend.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quotes")
@RequiredArgsConstructor
public class QuoteController {
    private final QuoteService quoteService;

    @PostMapping
    public ResponseEntity<QuoteResponseDto> create(@RequestBody @Valid QuoteCreateDto dto){
        Quote q = quoteService.createQuote(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(QuoteMapper.toDto(q));
    }

    @GetMapping
    public List<QuoteResponseDto> getAllQuotes() {
        return quoteService.getAllQuotes().stream()
                .map(QuoteMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponseDto> getById(@PathVariable Long id) {
        Quote quote = quoteService.getQuoteById(id);
        return ResponseEntity.ok(QuoteMapper.toDto(quote));
    }

    @GetMapping("/brand/{brandId}")
    public List<QuoteResponseDto> getQuotesByBrand(@PathVariable Long brandId) {
        return quoteService.getQuotesByBrand(brandId).stream()
                .map(QuoteMapper::toDto)
                .toList();
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuoteResponseDto> update(@RequestBody @Valid QuoteUpdateDto dto,
                                                   @PathVariable Long id){
        Quote q = quoteService.updateQuote(id,dto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(QuoteMapper.toDto(q));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        quoteService.deleteQuote(id);
    }

    @PatchMapping("/{id}/expire")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void expireQuote(@PathVariable Long id) {
        quoteService.expireQuote(id);
    }
}
