package com.enliple.ar.jpa.db1.key;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class SupplementDtoKey implements Serializable {
    @Column(name="statsDay")
    private int statsDay;

    @Column(name="adverId")
    private String adverId;

    public SupplementDtoKey(int statsDay,String adverId,String productCodes) {
        this.statsDay = statsDay;
        this.adverId = adverId;
    }
}
