package com.enliple.ar.jpa.db2.key;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class JobResultArKey implements Serializable {
    @Column(name = "advId")
    private String advId;

    @Column(name = "type")
    private String type;

    public JobResultArKey() {

    }

    @Builder
    public JobResultArKey(String advId,String type) {
        this.advId = advId;
        this.type = type;
    }
}
