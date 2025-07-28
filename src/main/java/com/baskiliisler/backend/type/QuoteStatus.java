package com.baskiliisler.backend.type;

public enum QuoteStatus {
    DRAFT,              // taslak – satış temsilcisi dolduruyor
    OFFER_SENT,         // markaya iletildi
    ACCEPTED,           // marka onayladı → siparişe dönebilir
    DECLINED,           // marka reddetti
    EXPIRED             // validUntil süresi geçti, otomatik
}

