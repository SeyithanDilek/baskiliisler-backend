package com.baskiliisler.backend.dto;

import java.time.LocalDate;
import java.util.Map;

public record QuoteAcceptDto(Map<Long, LocalDate> itemDeadlines) {}
