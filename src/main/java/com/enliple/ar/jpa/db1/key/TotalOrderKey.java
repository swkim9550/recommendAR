package com.enliple.ar.jpa.db1.key;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class TotalOrderKey implements Serializable {

    @Column(name = "adverId")
    private String adverId;

    @Column(name = "cnt")
    private String cnt;

    public TotalOrderKey() {

    }
}
