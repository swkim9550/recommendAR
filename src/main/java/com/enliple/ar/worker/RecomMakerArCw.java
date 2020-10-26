package com.enliple.ar.worker;

import com.enliple.ar.common.CommonConstants;
import com.enliple.ar.common.Config;
import com.enliple.ar.dao.RedisCluster;
import com.enliple.ar.jpa.db1.domain.*;
import com.enliple.ar.jpa.db1.dto.CartArCountDto;
import com.enliple.ar.jpa.db1.service.SupplementCwService;
import com.enliple.ar.jpa.db2.domain.JobResultAr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component("AR_CW_DATA")
@Slf4j
public class RecomMakerArCw {

    @Resource(name = "AR_CW_DATA_JPA")
    private SupplementCwService supplementCwService;

    /**
     * 30일치 동시 장바구니 데이터 조회 후 보완재 도메인을 실행하여 추천 데이터를 추출.
     * @param adverId
     * @param currentIndex
     * @param maxIndex
     */
    public void makeArCwRecomData(String adverId, int currentIndex, int maxIndex) {
        JobResultAr jobResultAr = new JobResultAr();
        jobResultAr.setCreateTime(LocalDateTime.now());
        long start = System.currentTimeMillis();

        CartArCountDto cartArCountDto = supplementCwService.saveRedisSupplement(adverId, CommonConstants.CART_TYPE_SAME);
        int cartCountByAdverId = cartArCountDto.getCartCountByAdverId();
        int cartArRecomCountByAdverId = cartArCountDto.getCartArRecomCountByAdverId();

        long end = System.currentTimeMillis();
        log.info("{}-Calculate end. adverId:{}({}/{}). selectDBCounnt:{}. saveRedisCount:{}. batchTime:{}",
                CommonConstants.CART_TYPE_SAME, adverId, currentIndex, maxIndex, cartCountByAdverId, cartArRecomCountByAdverId, (end - start) / 1000.0 + "sec");
    }
}
