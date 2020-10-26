package com.enliple.ar.jpa.db1.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SupplementDto {

    private String statsDay;
    private String adverId;
    private String standardProductCode;
    private String recommendProductCode;
    private String purchaseType;
    private String reliabilitySet;
    private int recommendCount;
}
