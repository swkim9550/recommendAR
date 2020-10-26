package com.enliple.ar.jpa.db1.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class TempSupplementDto {
    private String adverId;
    private String produceSet;
    private String p1;
    private String p2;
    private int a;
    private int b;
    private int c;
    private int d;
    private String singleSupport1;
    private String singleSupport2;
    private String support;
    private double reliability1;
    private double reliability2;
}
