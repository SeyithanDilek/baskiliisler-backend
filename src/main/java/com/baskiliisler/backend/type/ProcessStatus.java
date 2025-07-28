package com.baskiliisler.backend.type;

import java.util.Map;
import java.util.Set;

public enum ProcessStatus {
    INIT,
    SAMPLE_LEFT,
    OFFER_SENT,
    ORDER_PLACED,
    SENT_TO_FACTORY,
    DELIVERED,
    CANCELLED,
    EXPIRED,
    COMPLETED;

    private static final Map<ProcessStatus, Set<ProcessStatus>> MATRIX = Map.of(
            INIT,             Set.of(SAMPLE_LEFT, CANCELLED),
            SAMPLE_LEFT,      Set.of(OFFER_SENT, CANCELLED),
            OFFER_SENT,       Set.of(ORDER_PLACED, EXPIRED, CANCELLED),
            ORDER_PLACED,     Set.of(SENT_TO_FACTORY, CANCELLED),
            SENT_TO_FACTORY,  Set.of(COMPLETED, CANCELLED),
            COMPLETED,        Set.of(),
            EXPIRED,          Set.of(),
            CANCELLED,        Set.of()
    );

    public boolean canTransitionTo(ProcessStatus target) {
        return MATRIX.getOrDefault(this, Set.of()).contains(target);
    }
}
