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

    @PutMapping("/{id}")
    public ResponseEntity<QuoteResponseDto> update(@RequestBody @Valid QuoteUpdateDto dto,
                                                   @PathVariable Long id){
        Quote q = quoteService.updateQuote(id,dto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(QuoteMapper.toDto(q));
    }
}
