package com.enliple.ar.jpa.db1.key;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;
import java.io.Serializable;

@Data
@Embeddable
public class ArKey implements Serializable {

    @Column(name="statsDay")
    private String statsDay;

    @Column(name="adverId")
    private String adverId;

    @Lob
    @Column(name="productCodes")
    private String productCodes;


    public ArKey() {
    }
}
