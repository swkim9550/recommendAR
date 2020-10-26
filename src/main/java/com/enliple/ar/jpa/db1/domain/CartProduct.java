package com.enliple.ar.jpa.db1.domain;

import com.enliple.ar.jpa.db1.key.ArKey;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name="ADVER_CART_PRDT")
public class CartProduct implements Serializable {

    @EmbeddedId
    private ArKey key;

    public CartProduct() {

    }
}
