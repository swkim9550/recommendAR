package com.enliple.ar.util;
/*
 * COPYRIGHT (c) Enliple 2019
 * This software is the proprietary of Enliple
 *
 * @author <a href=“mailto:sywon@enliple.com“>sywon</a>
 * @since 2019-10-23
 */



import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * create on 2019-09-19.
 * <p> 클래스 명 : 변환 유틸 </p>
 * <p> 클래스 용도 : 데이터 생성 및 변환 관련 클래스 </p>
 *
 * @author sywon
 * @version 1.0
 */
@Slf4j
public class ConvertUtils {

    public static final String FORMAT_YYYYMMDD = "yyyyMMdd";

    private ConvertUtils() {
    }


    /**
     * <pre>
     * 기능 명 : 주문번호 생성.
     * 기능 용도 : 주문번호를 생성한다.
     * </pre>
     *
     * @param keyIp 클라이언트 IP
     * @param txt   주문번호 구별 글자
     * @return 주문번호
     */


    /**
     * <pre>
     * 기능 명 : 달력 생성.
     * 기능 용도 : 해당 포맷의 날짜 반환한다
     * </pre>
     *
     * @param format 추출할 날에 대한 포맷.
     * @return 날짜
     */
    public static String getFormatDate(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = new Date();
        return sdf.format(date);
    }

    /**
     * <pre>
     *   기능 명 : 날짜 생성
     *   기능 용도 : 해당 포맷의 날짜를 반환한다.
     * </pre>
     *
     * @param format 달력 포맷.
     * @param beforeCnt 기준 일
     * @return
     */
    public static String getFormatDate(String format, int beforeCnt) {
        Calendar c = new GregorianCalendar();
        c.add(Calendar.DATE, beforeCnt);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(c.getTime());
    }

    /**
     * <pre>
     *   기능 명 :가격 변환
     *   기능 용도 : 콤마나 .00 식으로 되어있는 데이터를 변환한다.
     * </pre>
     *
     * @param value 변환할 가격
     * @return
     */

    /**
     * <pre>
     *   기능 명 : 규칙에 맞게 변환
     *   기능 용도 : 자바 규칙에 위반되는 문자열(_)을 카멜표현식으로 변경한다.
     * </pre>
     *
     * @param value 전환할 값
     * @return
     */
    public static String underscoreToCamel(String value) {
        int len = value.length();

        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;

        for (int i = 0; i < len; i++) {
            char microValue = value.charAt(i);
            if (microValue == '_' || microValue == '-') {
                nextUpper = i > 0;
            } else {
                if (nextUpper) {
                    sb.append(Character.toUpperCase(microValue));
                    nextUpper = false;
                } else {
                    sb.append(Character.toLowerCase(microValue));
                }
            }
        }

        return sb.toString();
    }

    /**
     * <pre>
     *   기능 명 : 괄호 치환.
     *   기능 용도 : 특수문자로 변환된 괄호를 다시 괄호로 치환한다.
     * </pre>
     *
     * @param val 문자열
     * @return
     */
    public static String replaceBracket(String val) {
        if (val.indexOf("&lb;") > -1 && val.indexOf("&gb;") > -1) {
            val = val.replace("&lb;", "[");
            val = val.replace("&gb;", "]");
        }
        return val;
    }

    /**
     * <pre>
     * ip hashCode 변환
     * 해쉬 처리한 ip를 반환.
     * </pre>
     *
     * @param keyIp 클라이언트 IP
     * @return 해쉬코드
     */
    private static String getKeyIpHashCode(String keyIp) {
        int hashCode = keyIp.hashCode();
        return (hashCode > -1 ? "0" : 1) + String.format("%010d", Math.abs(hashCode));
    }

    /**
     * <pre>
     *   기능 명 : 주문번호 시간 조절
     *   기능 용도 : 5분 간격으로 주문번호의 시간을 조절한다.
     * </pre>
     * @param minute 시간
     * @return
     */
    private static String getMinuteDistance(int minute) {
        String result = "";
        if (minute >= 55) {
            result = "55";
        } else if (minute >= 50) {
            result = "50";
        } else if (minute >= 45) {
            result = "45";
        } else if (minute >= 40) {
            result = "40";
        } else if (minute >= 35) {
            result = "35";
        } else if (minute >= 30) {
            result = "30";
        } else if (minute >= 25) {
            result = "25";
        } else if (minute >= 20) {
            result = "20";
        } else if (minute >= 15) {
            result = "15";
        } else if (minute >= 10) {
            result = "10";
        } else if (minute >= 5) {
            result = "05";
        } else {
            result = "00";
        }
        return result;
    }


    public static String nvl(String nvl) {
        return nvl(nvl, "");
    }

    public static String nvl(String nvl, String rep) {
        return nvl != null ? nvl.trim() : rep;
    }
}

