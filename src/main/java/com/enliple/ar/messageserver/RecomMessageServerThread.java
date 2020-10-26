package com.enliple.ar.messageserver;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;

@Slf4j
@Component
public class RecomMessageServerThread {
    private final String USER_AGENT = "Mozilla/5.0";

    @Async
    public void socketResponse(Socket sock) {
        BufferedReader br = null;
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(),"UTF-8"));
            br = new BufferedReader(new InputStreamReader(sock.getInputStream(),"UTF-8"));
            String cmd = br.readLine();
            if(StringUtils.isNotEmpty(cmd)) {
                cmd = cmd.startsWith("/")?cmd.replace("/", ""):cmd;
                cmd = cmd.toLowerCase();
                log.info("getCommandMessage : "+cmd);
                if(cmd.equals("memory") || cmd.equals("mem")|| cmd.equals("메모리")) {
                    Runtime runtime = Runtime.getRuntime();
                    int sizeGB = 1024*1024*1024;
                    int sizeMB = 1024*1024;
                    long curMem = (runtime.totalMemory() - runtime.freeMemory());
                    String text = "";
                    if(curMem > sizeGB) {
                        text = ("메모리 사용량 : "+(curMem/sizeGB)+"GB");
                    }else {
                        text = ("메모리 사용량 : "+(curMem/sizeMB)+"MB");
                    }
                    pw.println(text);
                }
            }
            pw.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            if(pw != null) {
                try {
                    pw.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(sock != null) {
                try {
                    sock.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    @Async
    public void telegramMessageSend(TelegramMessageDto telegramMessageDao) {
        HttpURLConnection con = null;
        try {
            URL url = new URL(telegramMessageDao.getSendUrl());
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            // HTTP POST 메소드 설정
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setDoOutput(true); // POST 파라미터 전달을 위한 설정

            OutputStream wr = con.getOutputStream();
            // Send post request
            //DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(("message="+telegramMessageDao.getMessage()).getBytes("euc-kr") );
            //wr.writeBytes(parameters);
            wr.flush();
            wr.close();
            //int responseCode = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        }catch(SocketException e) {
            log.warn("Telegram Alarm API Server closed. ["+telegramMessageDao.getMessage()+"] : "+(telegramMessageDao.getSendUrl()));
        }catch(Exception e) {
            log.error("",e);
        }
    }
}

