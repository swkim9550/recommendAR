package com.enliple.ar.jpa.db1.domain;


import com.enliple.ar.jpa.db1.key.TotalAdverIdKey;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;


@Getter
@Setter
@Entity
@Table(name="SAME_BUY_HIS")
public class TotalSameOrder implements Serializable {
    @EmbeddedId
    private TotalAdverIdKey key;

    public TotalSameOrder() {

    }

}
