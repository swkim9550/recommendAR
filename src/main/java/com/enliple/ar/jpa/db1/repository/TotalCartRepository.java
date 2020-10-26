package com.enliple.ar.jpa.db1.repository;

import com.enliple.ar.jpa.db1.domain.TotalCartAdverId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TotalCartRepository extends CrudRepository<TotalCartAdverId, Long> {

    @Query(nativeQuery=true, value = "SELECT\n" +
            "      ADVER_ID as adverId\n" +
            "      , COUNT(ADVER_ID) as cnt\n" +
            "    FROM ADVER_CART_PRDT\n" +
            "    WHERE STATS_DTTM BETWEEN DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -30 DAY),'%Y%m%d')\n" +
            "                          AND DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -1 DAY),'%Y%m%d')\n" +
            "    GROUP BY ADVER_ID")
    List<TotalCartAdverId> findAllTotalCartAdverIdList();

}
