package com.baskiliisler.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@MappedSuperclass
@Getter
@Setter
@FilterDef(
    name = "dealerFilter",
    parameters = @ParamDef(name = "dealerId", type = Long.class)
)
@Filter(name = "dealerFilter", condition = "dealer_id = :dealerId")
public abstract class BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")
    protected Dealer dealer;
} 