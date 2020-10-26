package com.enliple.ar.jpa.db1.domain;

import com.enliple.ar.jpa.db1.key.SuppRecommKey;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name="SUPP_RECOMM")
public class SuppRecomm implements Serializable {
    @EmbeddedId
    private SuppRecommKey key;

    @Column(name = "RECOMMEND_PRODUCT_CODE")
    private String RECOMMEND_PRODUCT_CODE;

    @Column(name = "RELIABILITY")
    private String RELIABILITY;

    @Column(name = "RECOMMEND_PRODUCT_QTY")
    private int RECOMMEND_PRODUCT_QTY;

    public SuppRecomm() {

    }
}