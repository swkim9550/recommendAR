package com.enliple.ar;

import com.enliple.ar.api.InsiteListApi;
import com.enliple.ar.common.CommonConstants;
import com.enliple.ar.common.Config;
import com.enliple.ar.dao.RedisCluster;
import com.enliple.ar.jpa.db1.domain.*;
import com.enliple.ar.jpa.db1.key.TotalAdverIdKey;
import com.enliple.ar.jpa.db1.service.SupplementCwService;
import com.enliple.ar.jpa.db1.service.SupplementService;
import com.enliple.ar.jpa.db2.domain.JobResultAr;
import com.enliple.ar.jpa.db2.key.JobResultArKey;
import com.enliple.ar.jpa.db2.service.JobResultService;
import com.enliple.ar.messageserver.RecomMessageServer;
import com.enliple.ar.worker.RecomMakerArCw;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AppRunner implements CommandLineRunner {
    private final String PURCHASE_TYPE_SAME = "10";
    private final String PURCHASE_TYPE_CONTI = "20";
    private final String SHOPBOT2 = "shopbot2";
    private final String TRUE = "true";
    private final String BATCH_ORDER = "order";
    private final String IS_INSITE = "insite";

    @Autowired
    private RecomMessageServer telegramServer;

    @Autowired
    private RedisCluster redisCluster;

    @Autowired
    private InsiteListApi insiteListApi;

    @Autowired
    private Config config;

    @Resource(name = "AR_DATA")
    private SupplementService supplementService;

    @Resource(name = "AR_CW_DATA_JPA")
    private SupplementCwService supplementCwService;
    @Resource(name = "AR_CW_DATA")
    private RecomMakerArCw recomMakerArCw;

    @Resource(name = "AR_RESULT")
    private JobResultService jobResultService;

    private List<String> insiteList;
    //비동기 스레드 테스트
//    public void method1(final String message) throws Exception {
//        executorService.submit(new Runnable() {
//            @Override
//            public void run() {
//                // do something
//            }
//        });
//    }

    @Override
    public void run(String... args) throws Exception {
        if(args.length == 0) {
            return;
        }

        try {
            log.info("::::::::::::AR batch start!:::::::::::");
            telegramServer.sendTelegramMessage("::::::::::::AR batch Start!:::::::::::");
            long start = System.currentTimeMillis();
            boolean isInsite = (args[0].equals(IS_INSITE)) ? true : false;
            if(isInsite) {
                insiteList = insiteListApi.getInsiteList();
//                insiteList = new ArrayList<>();
//                insiteList.add("cbti");
            }

            batchOrder(isInsite);
            batchCart(isInsite);

            long end = System.currentTimeMillis();
            log.info("::::::::::::AR batch end!:::::::::::{}", (end - start) / 1000.0 + "sec");
            telegramServer.sendTelegramMessage("::::::::::::AR batch end!::::::::::: "+ (end - start) / 1000.0 + "sec");
        }
        catch (Exception e){
            log.info(e.toString());
        }
        finally {
            this.close();
            log.info("ended");
        }
    }

    private void batchOrder(boolean isInsite) {
        int currentIndex = 1;
        int maxIndex = 1;
        //FILE LOG 로 강제로 밀어넣어 야할 경우에 사용.
        if(config.getFileLog().equals(TRUE)){ // TEST용 Shopbot2
            telegramServer.sendTelegramMessage("AR Shopbot2 batch start!");
            startArSameOrderBatch(makeTotalSameOger(SHOPBOT2), currentIndex, maxIndex);
        } else if(isInsite) { // 인사이트용 광고주만
            telegramServer.sendTelegramMessage("AR Insite SameOrder batch start!");
            maxIndex = insiteList.size();
            for (String adverId : insiteList) {
                startArSameOrderBatch(makeTotalSameOger(adverId), currentIndex, maxIndex);
                currentIndex++;
            }
            telegramServer.sendTelegramMessage("AR Insite SameOrder batch end!");
            telegramServer.sendTelegramMessage("AR Insite ContinuityOrders batch start!");
            currentIndex = 1;
            for (String adverId : insiteList) {
                startArContinuityOrderBatch(makeTotalContinuityOrder(adverId), currentIndex, maxIndex);
                currentIndex++;
            }
            telegramServer.sendTelegramMessage("AR Insite ContinuityOrders batch end!");
        } else { // 어제부터 30일동안 동시/연속 구매가 이뤄진 광고주만
            List<TotalSameOrder> totalSameOrders = supplementService.getTotalSameOrders();
            maxIndex = totalSameOrders.size();
            telegramServer.sendTelegramMessage("AR All SameOrder batch start!");
            if (!totalSameOrders.isEmpty()) {
                for (TotalSameOrder totalSameOrder : totalSameOrders) {
                    startArSameOrderBatch(totalSameOrder, currentIndex, maxIndex);
                    currentIndex++;
                }
            }
            telegramServer.sendTelegramMessage("AR All SameOrder batch end!");
            telegramServer.sendTelegramMessage("AR All ContinuityOrders batch start!");
            currentIndex = 1;
            List<TotalContinuityOrder> totalContinuityOrders = supplementService.getTotalContinuityOrders();
            maxIndex = totalContinuityOrders.size();
            if (!totalContinuityOrders.isEmpty()) {
                for(TotalContinuityOrder totalContinuityOrder : totalContinuityOrders) {
                    startArContinuityOrderBatch(totalContinuityOrder, currentIndex, maxIndex);
                    currentIndex++;
                }
            }
            telegramServer.sendTelegramMessage("AR All ContinuityOrders batch end!");
        }
    }

    private void startArSameOrderBatch(TotalSameOrder totalSameOrder, int currentIndex, int maxIndex){
        String adverId = totalSameOrder.getKey().getAdverId();
        /*
         * 0. 동시구매 활용 보완재 데이터 수집
         */
        /*
         * 1. 시작시간 기록.
         */
        JobResultAr jobResultAr = new JobResultAr();
        jobResultAr.setCreateTime(LocalDateTime.now());
        /*
         * 30일치 동시구매 데이터 조회 후 보완재 도메인을 실행하여 추천 데이터를 추출.
         */
        List<SameOrder> sameOrders = new ArrayList<>();
        long start = System.currentTimeMillis();
        long end = 0;
        int resultCount = 0;

        if(adverId.equals(SHOPBOT2)){
            sameOrders = supplementService.readLogFile(); //File Version
        }else{
            sameOrders = supplementService.getSameOrders(adverId);
        }

        if (!sameOrders.isEmpty()) {
            totalSameOrder.getKey().setCnt(String.valueOf(sameOrders.size()));
            Supplement sameSupplement = new Supplement(totalSameOrder, sameOrders, config);
            resultCount = sameSupplement.saveRedisSupplement(adverId, redisCluster);
        }
        insertSameJobResult(adverId,resultCount,jobResultAr, PURCHASE_TYPE_SAME);
        end = System.currentTimeMillis();
        log.info("{}-Calculate end. adverId:{}({}/{}). selectDBCounnt:{}. saveRedisCount:{}. batchTime:{}",
                CommonConstants.PURCHASE_TYPE_SAMEORDER, adverId, currentIndex, maxIndex, sameOrders.size(), resultCount, (end - start) / 1000.0 + "sec");
    }

    private void startArContinuityOrderBatch(TotalContinuityOrder totalContinuityOrder, int currentIndex, int maxIndex){
        String adverId = totalContinuityOrder.getKey().getAdverId();
        /*
         * 0. 연속구매 활용 보완재 데이터 수집
         */
        /*
         * 1. 시작시간 기록.
         */
        JobResultAr jobResultAr = new JobResultAr();
        jobResultAr.setCreateTime(LocalDateTime.now());
        /*
         * 30일치 연속구매 데이터 조회 후 보완재 도메인을 실행하여 추천 데이터를 추출.
         */
        List<ContinuityOrder> continuityOrders = supplementService.getContinuityOrder(adverId);
        long start = System.currentTimeMillis();
        long end = 0;
        int resultCount = 0;

        if (!continuityOrders.isEmpty()) {
            totalContinuityOrder.getKey().setCnt(String.valueOf(continuityOrders.size()));
            Supplement continuitySupplement = new Supplement(totalContinuityOrder, continuityOrders, config);
            resultCount = continuitySupplement.saveRedisSupplement(adverId, redisCluster);
        }
        insertContinuityJobResult(adverId,resultCount,jobResultAr,PURCHASE_TYPE_CONTI);
        end = System.currentTimeMillis();
        log.info("{}-Calculate end. adverId:{}({}/{}). selectDBCounnt:{}. saveRedisCount:{}. batchTime:{}",
                CommonConstants.PURCHASE_TYPE_CONTINUITYORDER, adverId, currentIndex, maxIndex, continuityOrders.size(), resultCount, (end - start) / 1000.0 + "sec");
    }

    private void insertContinuityJobResult(String adverId, int resultCount,JobResultAr jobResultAr,String type){
        JobResultArKey jobResultArKey = new JobResultArKey();
        jobResultArKey.setAdvId(adverId);
        jobResultArKey.setType(type);
        jobResultService.insertArResult(JobResultAr.builder().key(jobResultArKey)
                .status("D")
                .count(resultCount)
                .createTime(jobResultAr.getCreateTime())
                .updateTime(LocalDateTime.now()).build());
    }

    private void insertSameJobResult(String adverId, int resultCount,JobResultAr jobResultAr,String type){
        JobResultArKey jobResultArKey = new JobResultArKey();
        jobResultArKey.setAdvId(adverId);
        jobResultArKey.setType(type);
        jobResultService.insertArResult(JobResultAr.builder().key(jobResultArKey)
                .status("D")
                .count(resultCount)
                .createTime(jobResultAr.getCreateTime())
                .updateTime(LocalDateTime.now()).build());
    }

    private TotalSameOrder makeTotalSameOger(String adverId) {
        TotalSameOrder totalSameOrder = new TotalSameOrder();
        totalSameOrder.setKey(makeTotalAdverIdKey(adverId));
        return totalSameOrder;
    }

    private TotalContinuityOrder makeTotalContinuityOrder(String adverId) {
        TotalContinuityOrder totalContinuityOrder = new TotalContinuityOrder();
        totalContinuityOrder.setKey(makeTotalAdverIdKey(adverId));
        return totalContinuityOrder;
    }

    private TotalAdverIdKey makeTotalAdverIdKey(String adverId) {
        TotalAdverIdKey totalAdverIdKey = new TotalAdverIdKey();
        totalAdverIdKey.setAdverId(adverId);
        return totalAdverIdKey;
    }


    private void batchCart(boolean isInsite) {
        int currentIndex = 1;
        int maxIndex = 1;
        if(isInsite) { // 인사이트용 광고주만
            telegramServer.sendTelegramMessage("AR Insite SameCart batch start!");
            maxIndex = insiteList.size();
            for (String adverId : insiteList) {
                recomMakerArCw.makeArCwRecomData(adverId, currentIndex, maxIndex);
                currentIndex++;
            }
            telegramServer.sendTelegramMessage("AR Insite SameCart batch end!");
        } else { // 어제부터 30일동안 장바구니가 이뤄진 광고주만
            List<TotalCartAdverId> totalCartAdverIdList = supplementCwService.getTotalCartAdverIdList();
            maxIndex = totalCartAdverIdList.size();
            telegramServer.sendTelegramMessage("AR All SameCart batch start!");
            for (TotalCartAdverId totalCartAdverId : totalCartAdverIdList) {
                recomMakerArCw.makeArCwRecomData(totalCartAdverId.getKey().getAdverId(), currentIndex, maxIndex);
                currentIndex++;
            }
            telegramServer.sendTelegramMessage("AR All SameCart batch end!");
        }
    }

    private void close() {
        telegramServer.close();
        redisCluster.close();
    }
}