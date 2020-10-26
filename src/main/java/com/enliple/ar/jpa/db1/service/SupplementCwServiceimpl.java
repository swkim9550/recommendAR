package com.enliple.ar.jpa.db1.service;

import com.enliple.ar.common.CommonConstants;
import com.enliple.ar.common.Config;
import com.enliple.ar.dao.RedisCluster;
import com.enliple.ar.jpa.db1.domain.CartProduct;
import com.enliple.ar.jpa.db1.domain.TotalCartAdverId;
import com.enliple.ar.jpa.db1.dto.CartArCountDto;
import com.enliple.ar.jpa.db1.dto.SupplementDto;
import com.enliple.ar.jpa.db1.dto.TempSupplementDto;
import com.enliple.ar.jpa.db1.repository.SameCartRepository;
import com.enliple.ar.jpa.db1.repository.TotalCartRepository;
import com.enliple.ar.util.ConvertUtils;
import com.enliple.ar.util.DataUtils;
import com.enliple.ar.util.RedisKeyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component("AR_CW_DATA_JPA")
public class SupplementCwServiceimpl implements SupplementCwService {

    @Autowired
    private Config config;

    @Autowired
    private TotalCartRepository totalCartRepository;

    @Autowired
    private SameCartRepository sameCartRepository;

    @Autowired
    private RedisCluster redisCluster;

    private Map<String, Integer> onePcodeCartCntMap;
    private Map<String, Integer> twoPcodeCartCntMap;
    private String cartTypeCode = "";

    @Override
    public List<TotalCartAdverId> getTotalCartAdverIdList() {
        try{
            List<TotalCartAdverId> totalCartAdverIdList = totalCartRepository.findAllTotalCartAdverIdList();
            return totalCartAdverIdList;
        }catch (Exception e){
            log.info(e.toString());
            return null;
        }
    }

    @Override
    public CartArCountDto saveRedisSupplement(String adverId, String cartType) {
        boolean isSame = CommonConstants.CART_TYPE_SAME.equals(cartType);

        List<CartProduct> cartProductList;
        if(isSame) {
            cartProductList = getSameCartList(adverId);
            cartTypeCode = CommonConstants.CART_TYPE_SAME_CODE;
        } else {
            // 연속 장바구니는 추후
            cartProductList = new ArrayList<>();
        }
        int cartCountByAdverId = cartProductList.size();

        log.info("{}-Calculate start. adverId:{}. selectDBCounnt:{}", cartType, adverId, cartCountByAdverId);

        try {
            if(cartCountByAdverId > 0) {
                int cartArRecomCountByAdverId = makeCartArRecommend(adverId, cartCountByAdverId, cartProductList);

                return setCartArCountDto(cartCountByAdverId, cartArRecomCountByAdverId);
            } else {
                return setCartArCountDto(0, 0);
            }
        } catch (Exception e) {
            log.error(e.toString());
            return setCartArCountDto(0, 0);
        }
    }

    private int makeCartArRecommend(String adverId, int cartCountByAdverId, List<CartProduct> cartProductList) {
        List<SupplementDto> supplementDtoList = new ArrayList<>();
        onePcodeCartCntMap = new HashMap<>();
        twoPcodeCartCntMap = new HashMap<>();
        cartProductList.stream().forEach(
                cartProduct -> {
                    try {
                        String[] productCodesInCart = cartProduct.getKey().getProductCodes().split(CommonConstants.PIPE_REGEX);
                        String currentProductCode = "";
                        String compareProductCode = "";
                        int maxIndex = productCodesInCart.length;

                        for (int currentIndex = 0; currentIndex < maxIndex; currentIndex++) {
                            currentProductCode = productCodesInCart[currentIndex];
                            // 하나의 상품이 Cart에 담긴 횟수 (key : pcode1, value : 1)
                            if (onePcodeCartCntMap.get(currentProductCode) == null) {
                                onePcodeCartCntMap.put(currentProductCode, 1);
                            } else {
                                onePcodeCartCntMap.put(currentProductCode, onePcodeCartCntMap.get(currentProductCode) + 1);
                            }
                            for (int compareIndex = currentIndex + 1; compareIndex < maxIndex; compareIndex++) {
                                compareProductCode = productCodesInCart[compareIndex];
                                if (!currentProductCode.equals(compareProductCode)) {
                                    String key = currentProductCode + CommonConstants.COMMA_STRING + compareProductCode;
                                    String key2 = compareProductCode + CommonConstants.COMMA_STRING + currentProductCode;
                                    // 두 상품이 같이 Cart에 담긴 횟수
                                    if (twoPcodeCartCntMap.get(key) == null) {
                                        twoPcodeCartCntMap.put(key, 1);
                                        twoPcodeCartCntMap.put(key2, 1);
                                    } else {
                                        twoPcodeCartCntMap.put(key, twoPcodeCartCntMap.get(key) + 1);
                                        twoPcodeCartCntMap.put(key2, twoPcodeCartCntMap.get(key2) + 1);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("makeCartArRecommend()--"+e.toString());
                    }
                }
        );
        log.info("adverId:{} - 1", adverId);
        List<TempSupplementDto> calculatedSupplementDtoList = calcPcodeReliability(adverId, cartCountByAdverId);
        log.info("adverId:{} - 2", adverId);
        if(calculatedSupplementDtoList.size() > 0 ){
            DataUtils.tempDataSort(calculatedSupplementDtoList); // 정렬 처리.
            log.info("adverId:{} - 3", adverId);
            Map<String, List<String>> groupedProdcut1Map = DataUtils.groupingProduct1Map(calculatedSupplementDtoList); // p1 기준 그룹 정렬
            log.info("adverId:{} - 4", adverId);
            supplementDtoList = makeSupplementDtoAndSaveRedis(groupedProdcut1Map, adverId); // dto 생성 & redis 저장
            log.info("adverId:{} - 5", adverId);
        }
        return supplementDtoList.size();
    }

    private CartArCountDto setCartArCountDto(int cartCountByAdverId, int cartArRecomCountByAdverId) {
        CartArCountDto cartArCountDto = CartArCountDto.builder()
                .cartCountByAdverId(cartCountByAdverId)
                .cartArRecomCountByAdverId(cartArRecomCountByAdverId)
                .build();
        return cartArCountDto;
    }

    private List<TempSupplementDto> calcPcodeReliability(String adverId, int cartCountByAdverId) {
        List<TempSupplementDto> temps = new ArrayList<>();
        twoPcodeCartCntMap
                .keySet()
                .stream()
                .forEach(
                        key -> {
                            try {
                                String[] subKeys = key.split(CommonConstants.COMMA_STRING);
                                int pCode1CartCnt = onePcodeCartCntMap.get(subKeys[0]);
                                int pCode2CartCnt = onePcodeCartCntMap.get(subKeys[1]);
                                int twoPcodeCartCnt = twoPcodeCartCntMap.get(key); // 두 상품이 동시에 포함된 거래 수

                                if (twoPcodeCartCnt < 1){ // 동시포함 거래 수 1개 이상도 모두 사용(기획서의 조건1 변경)
                                    return;
                                }

                                double singleSupport1 = pCode1CartCnt / Double.valueOf(cartCountByAdverId); // 단일 지지도 (P1 기준)
                                double singleSupport2 = pCode2CartCnt / Double.valueOf(cartCountByAdverId);// 단일 지지도 (P2 기준)
                                double support = twoPcodeCartCnt / Double.valueOf(cartCountByAdverId); // 지지도
                                double reliability1 = support / singleSupport1; // 신뢰도1
                                double reliability2 = support / singleSupport2; // 신뢰도2

                                TempSupplementDto temp = TempSupplementDto.builder()
                                        .adverId(adverId)
                                        .produceSet(key)
                                        .p1(subKeys[0])
                                        .p2(subKeys[1])
                                        .a(cartCountByAdverId)
                                        .b(twoPcodeCartCnt)
                                        .c(pCode1CartCnt)
                                        .d(pCode2CartCnt)
                                        .singleSupport1(String.format("%.8f", singleSupport1))
                                        .singleSupport2(String.format("%.8f", singleSupport2))
                                        .support(String.format("%.8f", support))
                                        .reliability1(Double.parseDouble(String.format("%.8f", reliability1)))
                                        .reliability2(Double.parseDouble(String.format("%.8f", reliability2)))
                                        .build();
                                temps.add(temp);
                            }catch (Exception e){
                                log.error("calcPcodeReliability()--"+e.toString());
                            }
                        }
                );

        return temps;
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
    private List<SupplementDto> makeSupplementDtoAndSaveRedis(Map<String, List<String>> groupedProdcut1Map, String adverId) {
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
                                .purchaseType(cartTypeCode)
                                .recommendProductCode(String.join(CommonConstants.PIPE_STRING, recommendProducts))
                                .standardProductCode(pCode)
                                .reliabilitySet(String.join(CommonConstants.PIPE_STRING, reliabilitySet))
                                .recommendCount(recommendProducts.size())
                                .build();
                        supplementDtoList.add(supplementDto);
                        List<String> redisKeyList = RedisKeyUtils.makeRedisKey(cartTypeCode, supplementDto);
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

    private List<CartProduct> getSameCartList(String adverId) {
        try{
            List<CartProduct> cartProducts = sameCartRepository.findAllBySameCart(adverId);
            return cartProducts;
        }catch (Exception e){
            log.info(e.toString());
            return null;
        }
    }

}
