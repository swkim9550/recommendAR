package com.enliple.ar.jpa.db1.domain;

import com.enliple.ar.common.CommonConstants;
import com.enliple.ar.common.Config;
import com.enliple.ar.dao.RedisCluster;
import com.enliple.ar.jpa.db1.dto.SupplementDto;
import com.enliple.ar.jpa.db1.dto.TempSupplementDto;
import com.enliple.ar.util.ConvertUtils;
import com.enliple.ar.util.DataUtils;
import com.enliple.ar.util.RedisKeyUtils;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
@Getter
public class Supplement {

    private Config config;
    private String purchaseType;
    private String purchaseTypeCode;

    private TotalSameOrder totalSameOrder;
    private List<SameOrder> sameOrders;

    private TotalContinuityOrder totalContinuityOrder;
    private List<ContinuityOrder> continuityOrders;

    private Map<String, Integer> pCodePurchaseCntMap = new HashMap<>(); // 상품당 구매 횟수 맵 (key:상품코드, value:구매횟수)

    public Supplement() {

    }

    public Supplement(TotalSameOrder totalSameOrder, List<SameOrder> sameOrders, Config config) {
        this.totalSameOrder = totalSameOrder;
        this.sameOrders = sameOrders;
        this.config = config;
        this.purchaseType = CommonConstants.PURCHASE_TYPE_SAMEORDER;
        this.purchaseTypeCode = CommonConstants.PURCHASE_TYPE_SAMEORDER_CODE;
    }

    public Supplement(TotalContinuityOrder totalContinuityOrder,
                       List<ContinuityOrder> continuityOrders, Config config) {
        this.totalContinuityOrder = totalContinuityOrder;
        this.continuityOrders = continuityOrders;
        this.config = config;
        this.purchaseType = CommonConstants.PURCHASE_TYPE_CONTINUITYORDER;
        this.purchaseTypeCode = CommonConstants.PURCHASE_TYPE_CONTINUITYORDER_CODE;
    }

//    public static Supplement createSupplementBySamePurchase(TotalSameOrder order,
//                                                            List<SameOrder> sameOrders) {
//        return new Supplement(order, sameOrders);
//    }
//
//    public static Supplement createSupplementByContinuityPurchase(
//            TotalContinuityOrder totalContinuityOrder, List<ContinuityOrder> continuityOrders) {
//        return new Supplement(totalContinuityOrder, continuityOrders);
//    }

    /**
     * <pre>
     *   기능 명 : 보완재 데이터 수집
     *   기능 용도 : 보완재 데이터를 저장하는 로직
     * </pre>
     *
     * @return 저장할 Dto 개수
     */
    @Async("threadPoolTaskExecutor")
    public int saveRedisSupplement(String adverId, RedisCluster redisCluster) {
        List<SupplementDto> supplementDtoList = new ArrayList<>();
        boolean isSame = CommonConstants.PURCHASE_TYPE_SAMEORDER.equals(purchaseType);

        int totalCount = Integer.parseInt(isSame ?
                totalSameOrder.getKey().getCnt() : totalContinuityOrder.getKey().getCnt());

        try {
            log.info("{}-Calculate start. adverId:{}. selectDBCounnt:{}", purchaseType, adverId, totalCount);
            Map<String, Integer> twoPcodePurchaseCntMap =
                    isSame ? getTwoPcodePurchaseCntMapInSameOrder(pCodePurchaseCntMap) : getTwoPcodePurchaseCntMapInContiOrders(pCodePurchaseCntMap); // 동시 구매 횟수
            log.info("adverId:{} - 1", adverId);
            List<TempSupplementDto> tempSupplementDtoList = makeTempSupplementDtoList(pCodePurchaseCntMap, twoPcodePurchaseCntMap, adverId, totalCount); // 임시 데이터 생성
            log.info("adverId:{} - 2", adverId);
            if(tempSupplementDtoList.size() > 0 ){
                DataUtils.tempDataSort(tempSupplementDtoList); // 정렬 처리.
                log.info("adverId:{} - 3", adverId);
                Map<String, List<String>> groupedProdcut1Map = DataUtils.groupingProduct1Map(tempSupplementDtoList); // p1 기준 그룹 정렬
                log.info("adverId:{} - 4", adverId);
                supplementDtoList = makeSupplementDtoAndSaveRedis(groupedProdcut1Map, adverId, redisCluster); // dto 생성 & redis 저장
                log.info("adverId:{} - 5", adverId);
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return supplementDtoList.size();
    }

    /**
     * <pre>
     *   기능 명 : 동시 거래수 알고리즘
     *   기능 용도 : 동시 거래수를 구하기 위한 알고리즘
     * </pre>
     *
     * @returng
     */
    private Map getTwoPcodePurchaseCntMapInSameOrder(Map<String, Integer> pCodePurchaseCntMap) throws ExecutionException, InterruptedException {
        Map<String, Integer> twoProductPurchaseCntMap = new ConcurrentHashMap<>();
        sameOrders.stream().forEach(
                sameOrder -> {
                    try {
                        /*
                         * 1. 상품코드 값을 분리.
                         * ex) 1|2|3|4
                         */
                        String[] productCodesInSameOrder = sameOrder.getKey().getProductCodes().split(CommonConstants.PIPE_REGEX);
                        String currentProductCode = "";
                        String compareProductCode = "";
                        int maxIndex = productCodesInSameOrder.length;

                        for (int currentIndex = 0; currentIndex < maxIndex; currentIndex++) {
                            currentProductCode = productCodesInSameOrder[currentIndex];
                            if (pCodePurchaseCntMap.get(currentProductCode) == null) {
                                pCodePurchaseCntMap.put(currentProductCode, 1);
                            } else {
                                pCodePurchaseCntMap.put(currentProductCode, pCodePurchaseCntMap.get(currentProductCode) + 1);
                            }
                            for (int compareIndex = currentIndex + 1; compareIndex < maxIndex; compareIndex++) {
                                compareProductCode = productCodesInSameOrder[compareIndex];
                                if (!currentProductCode.equals(compareProductCode) && !currentProductCode.isEmpty() && !compareProductCode.isEmpty()) {
                                    String key = currentProductCode + CommonConstants.COMMA_STRING + compareProductCode;
                                    String key2 = compareProductCode + CommonConstants.COMMA_STRING + currentProductCode;

                                    if (twoProductPurchaseCntMap.get(key) == null) {
                                        twoProductPurchaseCntMap.put(key, 1);
                                        twoProductPurchaseCntMap.put(key2, 1);
                                    } else {
                                        twoProductPurchaseCntMap.put(key, twoProductPurchaseCntMap.get(key) + 1);
                                        twoProductPurchaseCntMap.put(key2, twoProductPurchaseCntMap.get(key2) + 1);
                                    }
                                }
                            }
                        }
//                        String[] productCodesInSameOrder = sameOrder.getKey().getProductCodes().split(CommonConstants.PIPE_REGEX);
//                        String currentProductCode = "";
//                        String nextProductCode = "";
//                        int maxIndex = productCodesInSameOrder.length;
//
//                        for (int currentIndex = 0; currentIndex < maxIndex; currentIndex++) {
//                            currentProductCode = productCodesInSameOrder[currentIndex];
//                            if (pCodePurchaseCntMap.get(currentProductCode) == null) {
//                                pCodePurchaseCntMap.put(currentProductCode, 1);
//                            } else {
//                                pCodePurchaseCntMap.put(currentProductCode, pCodePurchaseCntMap.get(currentProductCode) + 1);
//                            }
//                            for (int compareIndex = 1; compareIndex < maxIndex; compareIndex++) {
//                                nextProductCode = productCodesInSameOrder[compareIndex];
//                                if (currentIndex < compareIndex && currentIndex != compareIndex
//                                        && StringUtils.isNotEmpty(currentProductCode)
//                                        && StringUtils.isNotEmpty(nextProductCode)
//                                        && !currentProductCode.equals(nextProductCode)) {
//
//                                    String key = currentProductCode + "," + nextProductCode;
//                                    String key2 = nextProductCode + "," + currentProductCode;
//
//                                    if (nextProductCode.compareTo(currentProductCode) > 0) {
//                                        if (resultMap.get(key) == null) {
//                                            resultMap.put(key, 1);
//                                            resultMap.put(key2, 1);
//                                        } else {
//                                            resultMap.put(key, resultMap.get(key) + 1);
//                                            resultMap.put(key2, resultMap.get(key2) + 1);
//                                        }
//                                    }
//                                }
//                            }
//                        }
                    } catch (Exception e) {
                        log.error("getTwoPcodePurchaseCntMapInSameOrder()--"+e.toString());
                    }
                }
        );

        return twoProductPurchaseCntMap;
    }

    /**
     * <pre>
     *   기능 명 : 연속구매 실험군 대조군 간 카운트 맵 반환
     *   기능 용도 : 두상품코드의 동시포함 거래수를 구한다.
     * </pre>
     *
     * @return
     */
    //parallelStream()
    public Map<String, Integer> getTwoPcodePurchaseCntMapInContiOrders(Map<String, Integer> pCodePurchaseCntMap) {
        Map<String, Integer> twoProductPurchaseCntMap = new ConcurrentHashMap<>();
        continuityOrders.stream().forEach(
                continuityOrder -> {
                    try {
                        String[] productCodesInContiOrder = continuityOrder.getKey().getProductCodes().split(CommonConstants.PIPE_REGEX);
                        String currentProductCode = CommonConstants.EMPTY_STRING;
                        String compareProductCode = CommonConstants.EMPTY_STRING;
                        int maxIndex = productCodesInContiOrder.length;

                        for (int currentIndex = 0; currentIndex < maxIndex; currentIndex++) {
                            currentProductCode = productCodesInContiOrder[currentIndex];
                            if (pCodePurchaseCntMap.get(currentProductCode) == null) {
                                pCodePurchaseCntMap.put(currentProductCode, 1);
                            } else {
                                pCodePurchaseCntMap.put(currentProductCode, pCodePurchaseCntMap.get(currentProductCode) + 1);
                            }
                            for (int compareIndex = currentIndex + 1; compareIndex < maxIndex; compareIndex++) {
                                compareProductCode = productCodesInContiOrder[compareIndex];
                                if (!currentProductCode.equals(compareProductCode) && !currentProductCode.isEmpty() && !compareProductCode.isEmpty()) {
                                    String key = currentProductCode + CommonConstants.COMMA_STRING + compareProductCode;
                                    String key2 = compareProductCode + CommonConstants.COMMA_STRING + currentProductCode;

                                    if (twoProductPurchaseCntMap.get(key) == null) {
                                        twoProductPurchaseCntMap.put(key, 1);
                                        twoProductPurchaseCntMap.put(key2, 1);
                                    } else {
                                        twoProductPurchaseCntMap.put(key, twoProductPurchaseCntMap.get(key) + 1);
                                        twoProductPurchaseCntMap.put(key2, twoProductPurchaseCntMap.get(key2) + 1);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("getTwoPcodePurchaseCntMapInContiOrders()--"+e.toString());
                    }
                }

        );
        return twoProductPurchaseCntMap;
    }


    /**
     * <pre>
     *   기능 명 : 임시 데이터 생성
     *   기능 용도 : 각 상품의 구매 횟수 데이터와 두 상품(A,B)의 (동시/연속)구매 횟수 데이터로 보완재 저장을 위한 임시 데이터를 생성한다.
     * </pre>
     *
     * @param pCodePurchaseCntMap     각 상품의 구매 횟수 데이터
     * @param twoPcodePurchaseCntMap 두 상품의 (동시/연속)구매 횟수 데이터
     * @param adverId
     * @param totalCount
     * @return
     */
    private List<TempSupplementDto> makeTempSupplementDtoList(Map<String, Integer> pCodePurchaseCntMap,
                                                              Map<String, Integer> twoPcodePurchaseCntMap, String adverId, int totalCount) {
        List<TempSupplementDto> temps = new ArrayList<>();
        twoPcodePurchaseCntMap
                .keySet()
                .stream()
                .forEach(
                        key -> {
                            try {
                                //Thread.sleep(1);
                                String[] subKeys = key.split(CommonConstants.COMMA_STRING);
                                int pCode1PurchaseCnt = pCodePurchaseCntMap.get(subKeys[0]);
                                int pCode2PurchaseCnt = pCodePurchaseCntMap.get(subKeys[1]);
                                int twoPcodePurchaseCnt = twoPcodePurchaseCntMap.get(key); // 두 상품이 동시에 포함된 거래 수

                                if (twoPcodePurchaseCnt < 1){ // 동시포함 거래 수 1개 이상도 모두 사용(기획서의 조건1 변경)
                                    return;
                                }

                                double singleSupport1 = pCode1PurchaseCnt / Double.valueOf(totalCount); // 단일 지지도 (P1 기준)
                                double singleSupport2 = pCode2PurchaseCnt / Double.valueOf(totalCount);// 단일 지지도 (P2 기준)
                                double support = twoPcodePurchaseCnt / Double.valueOf(totalCount); // 지지도
//                                    double reliability1 = twoPcodePurchaseCnt / Double.valueOf(pCode1PurchaseCnt); // 신뢰도1
//                                    double reliability2 = twoPcodePurchaseCnt / Double.valueOf(pCode2PurchaseCnt); // 신뢰도2
                                double reliability1 = support / singleSupport1; // 신뢰도1
                                double reliability2 = support / singleSupport2; // 신뢰도2

                                TempSupplementDto temp = TempSupplementDto.builder()
                                        .adverId(adverId)
                                        .produceSet(key)
                                        .p1(subKeys[0])
                                        .p2(subKeys[1])
                                        .a(totalCount)
                                        .b(twoPcodePurchaseCnt)
                                        .c(pCode1PurchaseCnt)
                                        .d(pCode2PurchaseCnt)
                                        .singleSupport1(String.format("%.8f", singleSupport1))
                                        .singleSupport2(String.format("%.8f", singleSupport2))
                                        .support(String.format("%.8f", support))
                                        .reliability1(Double.parseDouble(String.format("%.8f", reliability1)))
                                        .reliability2(Double.parseDouble(String.format("%.8f", reliability2)))
                                        .build();
//                                    int c1 = count * value;
//                                    int c2 = (Integer.parseInt(p1) * Integer.parseInt(p2));

                                //20200818 규정제거
//                                    if (value >= 3
//                                            && (c1 / c2) > 1
//                                            && reliability1 > 0 && reliability2 > 0) {
//                                        temps.add(temp);
//                                    }
                                temps.add(temp);
                            }catch (Exception e){
                                log.error("makeTempSupplementDtoList()--"+e.toString());
                            }
                        }
                );

        return temps;
    }


    /**
     * <pre>
     *   기능 명 : 맵 병합
     *   기능 용도 : 두가지 맵을 병합처리한다.
     * </pre>
     *
     * @param map1
     * @param map2
     * @return
     */
    private Map<String,List<String>> mergeMap(Map<String, List<String>> map1, Map<String, List<String>> map2) {
        Map<String, List<String>> totalMap = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : map1.entrySet()) {
            List<String> list = entry.getValue();

            if (map2.get(entry.getKey()) != null) {
                list.addAll(map2.get(entry.getKey()));
            }
            totalMap.put(entry.getKey(), list);
        }

        return totalMap;
    }

    /**
     * <pre>
     *   기능 명 : 보완재를 레디스에 저장 후 반환
     *   기능 용도 : 보완재 저장을 위한 dto 목록을 만들어 레디스에 저장 후 반환.
     * </pre>
     *
     * @param groupedProdcut1Map     데이터
     * @param adverId      광고주아이디
     * @return
     */
    private List<SupplementDto> makeSupplementDtoAndSaveRedis(Map<String, List<String>> groupedProdcut1Map,
                                                              String adverId, RedisCluster redisCluster) {
        List<SupplementDto> supplementDtoList = new ArrayList<>();

        groupedProdcut1Map.keySet().stream().forEach(
                pCode -> {
                    try {
                        List<String> recommendProducts = new ArrayList<>();
                        List<String> reliabilitySet = new ArrayList<>();

                        for (String groupedProduct : groupedProdcut1Map.get(pCode)) {
                            String[] infoOfProduct = groupedProduct.split(CommonConstants.PIPE_REGEX);
                            recommendProducts.add(infoOfProduct[0].trim()); // 추천 상품들
                            reliabilitySet.add(infoOfProduct[1].trim()); // 추천 상품들과의 신뢰도1, 신뢰도2
                            if (recommendProducts.size() == 20) { // 2020.08.31 cwpark 10개에서 20개로 변경.
                                break;
                            }
                        }

                        SupplementDto supplementDto = SupplementDto.builder()
                                .adverId(adverId)
                                .statsDay(ConvertUtils.getFormatDate(ConvertUtils.FORMAT_YYYYMMDD))
                                .purchaseType(purchaseTypeCode)
                                .recommendProductCode(String.join(CommonConstants.PIPE_STRING, recommendProducts))
                                .standardProductCode(pCode)
                                .reliabilitySet(String.join(CommonConstants.PIPE_STRING, reliabilitySet))
                                .recommendCount(recommendProducts.size())
                                .build();
                        supplementDtoList.add(supplementDto);
                        List<String> redisKeyList = RedisKeyUtils.makeRedisKey(purchaseTypeCode, supplementDto);
                        if(redisKeyList.size() > 0) {
                            redisCluster.saveRedis(redisKeyList.get(0), supplementDto.getRecommendProductCode(), config.getRedisExpireTime());
                            redisCluster.saveRedis(redisKeyList.get(1), supplementDto.getReliabilitySet(), config.getRedisExpireTime());
                        }
                    }catch (Exception e){
                        log.error("makeSupplementDtoAndSaveRedis()--"+e.toString());
                    }
                }
        );

        return supplementDtoList;
    }
}
