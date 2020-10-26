package com.enliple.ar.jpa.db1.repository;

import com.enliple.ar.jpa.db1.domain.SuppRecomm;
import org.springframework.data.repository.CrudRepository;

public interface SupplementRepository extends CrudRepository<SuppRecomm, Long> {


//    @Query(nativeQuery=true, value ="INSERT\n" +
//            "      INTO\n" +
//            "        SUPP_RECOMM(\n" +
//            "        STATS_DTTM\n" +
//            "        , ADVER_ID\n" +
//            "        , STANDARD_PRODUCT_CODE\n" +
//            "        , RECOMMEND_PRODUCT_CODE\n" +
//            "        , PURCHASE_TYPE\n" +
//            "        , RELIABILITY\n" +
//            "        , RECOMMEND_PRODUCT_QTY\n" +
//            "      )VALUES(\n" +
//            "        #{statsDay}\n" +
//            "        , #{adverId}\n" +
//            "        , #{standardProductCode}\n" +
//            "        , #{recommendProductCode}\n" +
//            "        , #{purchaseType}\n" +
//            "        , #{reliabilitySet}\n" +
//            "        , #{recommendCount}\n" +
//            "      )")
//    int insertRecomData(SupplementDto dto);





}
