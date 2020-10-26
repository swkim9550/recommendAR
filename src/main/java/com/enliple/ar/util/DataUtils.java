/*
 * COPYRIGHT (c) Enliple 2019
 * This software is the proprietary of Enliple
 *
 * @author <a href=“mailto:cwpark@enliple.com“>cwpark</a>
 * @since 2020-09-02
 */
package com.enliple.ar.util;

import com.enliple.ar.common.CommonConstants;
import com.enliple.ar.jpa.db1.dto.TempSupplementDto;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * create on 2020-09-02.
 * <p> 클래스 설명 </p>
 * <p> {@link } and {@link }관련 클래스 </p>
 *
 * @author cwpark
 * @version 1.0
 * @see
 * @since 지원하는 자바버전 (ex : 5+ 5이상)
 */
public class DataUtils {

    /**
     * <pre>
     *   기능 명 : 오름차순 정렬
     *   기능 용도 : 보완재 데이터를 오름차순으로 정렬한다.
     * </pre>
     * @param dtos
     */
    public static void tempDataSort(List<TempSupplementDto> dtos) {
        Collections.sort(dtos, (tempSupplementDto1, tempSupplementDto2) -> {
            int desc = 0;

            if (tempSupplementDto1.getReliability1() == tempSupplementDto2.getReliability1()) {
                desc = String.valueOf(tempSupplementDto2.getReliability2())
                        .compareTo(String.valueOf(tempSupplementDto1.getReliability2()));
            } else {
                desc = String.valueOf(tempSupplementDto2.getReliability1())
                        .compareTo(String.valueOf(tempSupplementDto1.getReliability1()));
            }
            return desc;
        });
    }

    /**
     * <pre>
     *   기능명 : 그룹화
     *   기능 용도 : 상품코드 기준으로 그룹핑을 한다.
     * </pre>
     *
     * @param temps    임시데이터
     * @return
     */
    public static Map<String, List<String>> groupingProduct1Map(List<TempSupplementDto> temps) {
        Map<String, List<String>> groupedProdcut1Map = new HashMap<>();
        try{
            groupedProdcut1Map = temps.stream().collect(
                    Collectors.groupingBy(
                            c -> c.getP1(),
                            Collectors.mapping(
                                    c -> getSupplementValue(
                                            c.getP2()
                                            , c.getReliability1()
                                            , c.getReliability2()
                                    )
                                    , Collectors.toList()
                            )
                    )
            );
        return groupedProdcut1Map;
        }catch (Exception e){
            return groupedProdcut1Map;
        }
    }

    /**
     * <pre>
     *   기능 명 : 보완재 핵심데이터 묶음
     *   기능 용도 : 상품코드와 신뢰도1, 신뢰도2를 묶는다.
     * </pre>
     *
     * @param productCode  상품코드
     * @param reliability1 신뢰도1
     * @param reliability2 신뢰도2
     * @return
     */
    private static String getSupplementValue(String productCode, double reliability1, double reliability2) {
        StringBuilder sb = new StringBuilder();
        sb.append(productCode);
        sb.append(CommonConstants.PIPE_STRING);
        sb.append(reliability1);
        sb.append(CommonConstants.COMMA_STRING);
        sb.append(reliability2);
        return sb.toString();
    }

}