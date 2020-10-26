package com.enliple.ar.jpa.db1.repository;


import com.enliple.ar.jpa.db1.domain.ContinuityOrder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ContinuityRepository extends CrudRepository<ContinuityOrder, Long> {

    @Query(nativeQuery=true, value = "SELECT\n" +
            "      END_DTTM as statsDay\n" +
            "      , ADVER_ID as adverId\n" +
            "      , PRODUCT_CODES as productCodes\n" +
            "    FROM CONTI_BUY_HIS\n" +
            "    WHERE ADVER_ID =:adverId\n" +
            "    AND END_DTTM BETWEEN DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -30 DAY),'%Y%m%d')\n" +
            "                        AND DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -1 DAY),'%Y%m%d')")
    List<ContinuityOrder> findAllContinuityOrder(@Param("adverId") String adverId);
}
