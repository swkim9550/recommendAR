package com.enliple.ar.api;

import lombok.extern.slf4j.Slf4j;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class InsiteListApi {
    public List<String> getInsiteList(){
        List<String> instieList = new ArrayList<>();
        JSONParser parser = new JSONParser();

        try {
            String apiURL = "https://mbapi.mediacategory.com/advertiser/adverList";
            //String apiURL = "https://172.20.0.103/advertiser/adverList";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("token", "mbris_api");

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                Object obj = parser.parse(sb.toString());
                JSONObject jsonObject = (JSONObject) obj;

                JSONArray msg = (JSONArray) jsonObject.get("adverIdList");
                Iterator<String> iterator = msg.iterator();
                while (iterator.hasNext()) {
                    instieList.add(iterator.next());
                }
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            instieList.add("dabagirl"); //임시코드
//            String inputLine;
//            StringBuffer response = new StringBuffer();
//            while ((inputLine = br.readLine()) != null) {
//                response.append(inputLine);
//            }
//            br.close();
//            System.out.println(response.toString());
        } catch (Exception e) {
            System.out.println(e);
        }
        return instieList;
    }
}
