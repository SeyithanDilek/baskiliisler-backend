package com.baskiliisler.backend.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Unit {
    ADET("Adet"),
    KG("Kilogram"),
    METRE("Metre"),
    LITRE("Litre"),
    M2("Metrekare"),
    CM("Santimetre"),
    MM("Milimetre"),
    TON("Ton"),
    GRAM("Gram");

    private final String displayName;

    @Override
    public String toString() {
        return displayName;
    }
} 