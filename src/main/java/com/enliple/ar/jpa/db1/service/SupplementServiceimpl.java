package com.enliple.ar.jpa.db1.service;

import com.enliple.ar.common.CommonConstants;
import com.enliple.ar.common.Config;
import com.enliple.ar.jpa.db1.domain.*;
import com.enliple.ar.jpa.db1.dto.SupplementDto;
import com.enliple.ar.jpa.db1.key.ArKey;
import com.enliple.ar.jpa.db1.key.SuppRecommKey;
import com.enliple.ar.jpa.db1.repository.*;
import com.enliple.ar.jpa.db2.repository.JobResultArRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component("AR_DATA_JPA")
public class SupplementServiceimpl implements SupplementService {

    @Autowired
    private Config config;

    @Autowired
    private TotalSameOrderRepository totalSameOrderRepository;

    @Autowired
    private SameOrderRepository sameOrderRepository;

    @Autowired
    private TotalContinuityRepository totalContinuityRepository;

    @Autowired
    private ContinuityRepository continuityRepository;

    @Autowired
    private SupplementRepository supplementRepository;

    @Autowired
    private JobResultArRepository jobResultArRepository;

    @Override
    public List<TotalSameOrder> getTotalSameOrders() {
        try{
            List<TotalSameOrder> totalSameOrders = totalSameOrderRepository.findAllTotalSameOrder();
            return totalSameOrders;
        }catch (Exception e){
            log.info(e.toString());
            return null;
        }
    }

    @Override
    public List<SameOrder> getSameOrders(String adverId) {

        try{
            List<SameOrder> sameOrders = sameOrderRepository.findAllBySameOrder(adverId);
            return sameOrders;
        }catch (Exception e){
            log.info(e.toString());
            return null;
        }
    }

    @Override
    public List<TotalContinuityOrder> getTotalContinuityOrders() {
        try{
            List<TotalContinuityOrder> totalContinuityOrders = totalContinuityRepository.findAllTotalContinuity();
            return totalContinuityOrders;
        }catch (Exception e){
            log.info(e.toString());
            return null;
        }
    }

    @Override
    public List<ContinuityOrder> getContinuityOrder(String adverId){
        try{
            List<ContinuityOrder> continuityOrders = continuityRepository.findAllContinuityOrder(adverId);
            return continuityOrders;
        }catch (Exception e){
            log.info(e.toString());
            return null;
        }
    }

    @Override
    public int saveSupplement(SupplementDto dto) {
        try{
            //TODO 키 값 세팅.
            SuppRecommKey suppRecommKey = new SuppRecommKey();
            suppRecommKey.setADVER_ID(dto.getAdverId());
            suppRecommKey.setPURCHASE_TYPE(dto.getPurchaseType());
            suppRecommKey.setSTANDARD_PRODUCT_CODE(dto.getStandardProductCode());
            suppRecommKey.setSTATS_DTTM(dto.getStatsDay());

            SuppRecomm suppRecomm = new SuppRecomm();
            suppRecomm.setKey(suppRecommKey);
            suppRecomm.setRECOMMEND_PRODUCT_CODE(dto.getRecommendProductCode());
            suppRecomm.setRELIABILITY(dto.getReliabilitySet());
            suppRecomm.setRECOMMEND_PRODUCT_QTY(dto.getRecommendCount());

            supplementRepository.save(suppRecomm);
            return 0;
        }catch (Exception e){
            log.info(e.toString());
            return 0;
        }
    }

    @Override
    public List<SameOrder> readLogFile() {
        List<SameOrder> sameOrders = new ArrayList<>();
        ArKey arKey = new ArKey();
        SameOrder sameOrder = new SameOrder();
        //Sam
        String filePath = String.format("%s/%s", config.getRecomFilePath(),"lotteon.impm");

        BufferedReader br = null;
        FileReader fr = null;

        try{
            fr = new FileReader(filePath);
            br = new BufferedReader(fr);

            String line;
            line = br.readLine();
            for (int i =0 ; line != null ; line = br.readLine()){
                sameOrders = new ArrayList<>();
                i++;
                String[] logArray = line.split(CommonConstants.COMMA_STRING);

                arKey.setStatsDay(logArray[0]);
                arKey.setAdverId(logArray[1]);
                arKey.setProductCodes(logArray[2]);
                sameOrder.setKey(arKey);
                sameOrders.add(i,sameOrder);
            }
        }catch (Exception e){

        }finally {
            try {
                if (br != null) br.close();
                if (fr != null) fr.close();
            } catch( IOException e) {
                log.error("", e);
            }
        }
        return sameOrders;
    }
}
