package com.enliple.ar.jpa.db1.repository;

import com.enliple.ar.jpa.db1.domain.CartProduct;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SameCartRepository extends CrudRepository<CartProduct, Long> {

    @Query(nativeQuery=true, value ="SELECT\n" +
            "M.STATS_DTTM as statsDay\n" +
            ", M.ADVER_ID as adverId\n" +
            ", group_concat(M.PRODUCT_CODE separator '|') as productCodes\n" +
            "FROM \n" +
            "(\n" +
            "\tSELECT\n" +
            "\tSTATS_DTTM\n" +
            "\t, AU_ID\n" +
            "\t, ADVER_ID\n" +
            "\t, PRODUCT_CODE\n" +
            "\tFROM ADVER_CART_PRDT\n" +
            "\tWHERE ADVER_ID =:adverId\n" +
            "\tAND STATS_DTTM BETWEEN DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -30 DAY),'%Y%m%d')\n" +
            "\tAND DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -1 DAY),'%Y%m%d')\n" +
            "\tAND PRODUCT_CODE != ''\n" +
            "\tGROUP BY STATS_DTTM, AU_ID, PRODUCT_CODE\n" +
            ") AS M\n" +
            " GROUP BY STATS_DTTM, AU_ID\n" +
            "order by statsDay")
    List<CartProduct> findAllBySameCart(@Param("adverId") String adverId);
}
