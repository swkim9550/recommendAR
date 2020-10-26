/*
 * COPYRIGHT (c) Enliple 2019
 * This software is the proprietary of Enliple
 *
 * @author <a href=“mailto:cwpark@enliple.com“>cwpark</a>
 * @since 2020-09-02
 */
package com.enliple.ar.util;

import com.enliple.ar.common.CommonConstants;
import com.enliple.ar.jpa.db1.dto.SupplementDto;

import java.util.ArrayList;
import java.util.List;

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
public class RedisKeyUtils {

    /**
     * purchaseTypeCode에 따라 레디스 키를 생성
     * @param typeCode
     * @param supplementDto
     * @return 레디스 저장 키
     */
    public static List<String> makeRedisKey(String typeCode, SupplementDto supplementDto) {
        List<String> redisKeySet = new ArrayList<>();
        String keyPreFix;

        if (typeCode.equals(CommonConstants.PURCHASE_TYPE_SAMEORDER_CODE)) {
            keyPreFix = CommonConstants.PURCHASE_TYPE_SAMEORDER_REDIS_KEY_PREFIX;
        } else if (typeCode.equals(CommonConstants.PURCHASE_TYPE_CONTINUITYORDER_CODE)) {
            keyPreFix = CommonConstants.PURCHASE_TYPE_CONTINUITYORDER_REDIS_KEY_PREFIX;
        } else if (typeCode.equals(CommonConstants.CART_TYPE_SAME_CODE)) {
            keyPreFix = CommonConstants.CART_TYPE_SAME_REDIS_KEY_PREFIX;
        } else if (typeCode.equals(CommonConstants.CART_TYPE_CONTINUITY_CODE)) {
            keyPreFix = CommonConstants.CART_TYPE_CONTINUITY_REDIS_KEY_PREFIX;
        } else {
            return redisKeySet;
        }

        String adverId = supplementDto.getAdverId();
        String standardProductCode = supplementDto.getStandardProductCode();
        redisKeySet.add(keyPreFix + CommonConstants.UNDERSCORE_STRING + adverId + CommonConstants.UNDERSCORE_STRING +
                standardProductCode);
        redisKeySet.add(keyPreFix + CommonConstants.UNDERSCORE_STRING + adverId + CommonConstants.UNDERSCORE_STRING +
                standardProductCode + CommonConstants.UNDERSCORE_STRING + CommonConstants.STRING_RELIABILITY);
        return redisKeySet;
    }
}