package com.enliple.ar.jpa.db1.repository;

import com.enliple.ar.jpa.db1.domain.TotalSameOrder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TotalSameOrderRepository extends CrudRepository<TotalSameOrder, Long> {

//    SELECT
//    ADVER_ID as adverId
//    FROM SAME_BUY_HIS
//    WHERE STATS_DTTM BETWEEN DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -30 DAY),'%Y%m%d')
//    AND DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -1 DAY),'%Y%m%d')
//    GROUP BY ADVER_ID
//    @Query(nativeQuery=true, value = "SELECT p FROM TotalSameOrder as p WHERE p.statsDay BETWEEN DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -30 DAY),'%Y%m%d') AND DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -1 DAY),'%Y%m%d')")
//    List<TotalSameOrder> findAllBy();


    @Query(nativeQuery=true, value = "SELECT\n" +
            "      ADVER_ID as adverId\n" +
            "      , COUNT(1) as cnt\n" +
            "    FROM SAME_BUY_HIS\n" +
            "    WHERE STATS_DTTM BETWEEN DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -30 DAY),'%Y%m%d')\n" +
            "                          AND DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -1 DAY),'%Y%m%d')\n" +
            "    GROUP BY ADVER_ID")
    List<TotalSameOrder> findAllTotalSameOrder();


//    @Query(nativeQuery=true, value ="SELECT\n" +
//            "      STATS_DTTM as statsDay\n" +
//            "      , ADVER_ID as adverId\n" +
//            "      , PRODUCT_CODES as productCodes\n" +
//            "    FROM SAME_BUY_HIS\n" +
//            "    WHERE ADVER_ID ='100085'\n" +
//            "    AND STATS_DTTM BETWEEN DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -30 DAY),'%Y%m%d')\n" +
//            "                        AND DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -1 DAY),'%Y%m%d')" )
//    List<SameOrder> findAllBySameOrder();
}
