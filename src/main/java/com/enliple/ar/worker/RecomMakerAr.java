package com.enliple.ar.worker;

import com.enliple.ar.common.CommonConstants;
import com.enliple.ar.common.Config;
import com.enliple.ar.jpa.db1.domain.*;
import com.enliple.ar.jpa.db1.dto.SupplementDto;
import com.enliple.ar.jpa.db1.key.ArKey;
import com.enliple.ar.jpa.db1.service.SupplementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component("AR_DATA")
@Slf4j
public class RecomMakerAr implements SupplementService {

    @Resource(name = "AR_DATA_JPA")
    private SupplementService supplementService;

    @Autowired
    private Config config;

    @Override
    public List<TotalSameOrder> getTotalSameOrders() {
        List<TotalSameOrder> totalSameOrders = supplementService.getTotalSameOrders();
        return totalSameOrders;
    }

    @Override
    public List<SameOrder> getSameOrders(String adverId) {
        List<SameOrder> sameOrders = supplementService.getSameOrders(adverId);
        return sameOrders;
    }

    @Override
    public List<TotalContinuityOrder> getTotalContinuityOrders()
    {
        List<TotalContinuityOrder> totalContinuityOrders = supplementService.getTotalContinuityOrders();
        return totalContinuityOrders;
    }

    @Override
    public List<ContinuityOrder> getContinuityOrder(String adverId) {
        List<ContinuityOrder> continuityOrders = supplementService.getContinuityOrder(adverId);
        return continuityOrders;
    }

    @Override
    public int saveSupplement(SupplementDto dto) {
        int insertCount = supplementService.saveSupplement(dto);
        return insertCount;
    }

    @Override
    public List<SameOrder> readLogFile() {
        List<SameOrder> sameOrders = new ArrayList<>();
        ArKey arKey = new ArKey();
        SameOrder sameOrder = new SameOrder();
        //Sam
        String filePath = String.format("%s/%s", config.getRecomFilePath(),"213.txt");

        BufferedReader br = null;
        FileReader fr = null;

        try{
            fr = new FileReader(filePath);
            br = new BufferedReader(fr);

            String line;
            line = br.readLine();
            for (int i =0 ; line != null ; line = br.readLine()){

                String[] logArray = line.split(CommonConstants.COMMA_STRING);

                arKey.setStatsDay(logArray[0]);
                arKey.setAdverId(logArray[1]);
                arKey.setProductCodes(logArray[2]);
                sameOrder.setKey(arKey);
                sameOrders.add(i,sameOrder);
                i++;
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
