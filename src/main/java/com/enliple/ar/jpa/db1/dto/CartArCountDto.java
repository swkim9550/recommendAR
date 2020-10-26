package com.enliple.ar.jpa.db1.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CartArCountDto {

    @Builder.Default
    private int cartCountByAdverId = 0; //
    @Builder.Default
    private int cartArRecomCountByAdverId = 0;
}
