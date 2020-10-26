package com.enliple.ar.jpa.db1.key;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class SuppRecommKey implements Serializable {
    @Column(name = "ADVER_ID")
    private String ADVER_ID;

    @Column(name = "PURCHASE_TYPE")
    private String PURCHASE_TYPE;

    @Column(name = "STANDARD_PRODUCT_CODE")
    private String STANDARD_PRODUCT_CODE;

    @Column(name = "STATS_DTTM")
    private String STATS_DTTM;

    public SuppRecommKey() {

    }
}
