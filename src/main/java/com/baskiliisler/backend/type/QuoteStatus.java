package com.baskiliisler.backend.type;

public enum QuoteStatus {
    DRAFT,              // taslak – satış temsilcisi dolduruyor
    OFFER_SENT,         // markaya iletildi, bekliyor
    ACCEPTED,           // marka onayladı → siparişe dönebilir
    DECLINED,           // marka aktif olarak reddetti
    EXPIRED             // validUntil süresi geçti, otomatik (pasif red)
}

