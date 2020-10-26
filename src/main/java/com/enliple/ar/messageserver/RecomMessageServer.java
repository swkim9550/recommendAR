package com.enliple.ar.messageserver;

import com.enliple.ar.common.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

@Slf4j
@Component
public class RecomMessageServer implements Runnable {

    @Autowired
    private Config config;

    private boolean serverRun = true;

    @Autowired
    private RecomMessageServerThread telegramRequestServerThread;

    private ServerSocket server = null;

    @Value("${telegram.cfengine.notice.resturl}")
    private String targetNoticeUrl;

    @PostConstruct
    public void init() throws IOException {
        //this.app = app;
        try {
            String serverAddr = config.getTelegramServer();
            log.info("{} server starting",serverAddr);
            server = new ServerSocket(Integer.parseInt(serverAddr.split(":")[1].trim()));
            log.info("{} server started",serverAddr);
        }catch(Exception e) {
            e.printStackTrace();
            //App.sendTelegramExceptoinMessage("서버생성실패 : "+e.getLocalizedMessage());
        }
        //소켓 서버 시작
        new Thread(this).start();
    }

    @Override
    public void run() {
        if(server!=null) {
            try {
                do {
                    Socket sock = server.accept();
                    telegramRequestServerThread.socketResponse(sock);
                    log.info("waiting...");
                }while(serverRun);
            } catch (SocketException e) {
                log.info("server closed ");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                log.error("server : "+e);
                e.printStackTrace();
            }
        }
        log.info("server end...");
    }

    public void sendTelegramMessage(String message){
        if(config.isTelegramSend()) {
            telegramRequestServerThread.telegramMessageSend(TelegramMessageDto.builder()
                    .message(message)
                    .messageFlag(TelegramMessageDto.MESSAGE_NOTICE)
                    .sendUrl(targetNoticeUrl)
                    .build());
        }else {
            log.warn("[telgramMsg] "+message);
        }
    }

    @PreDestroy
    public void close() {
        try {
            this.serverRun = false;
            server.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log.warn("RecomServer downing");
    }
}
