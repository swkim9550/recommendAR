package com.enliple.ar.messageserver;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TelegramMessageDto {
    public final static int MESSAGE_NOTICE = 0;

    public final static int MESSAGE_EXCEPTION = 1;

    private String message;

    private String sendUrl;

    private int messageFlag=-1;

    @Builder
    public TelegramMessageDto(int messageFlag, String message, String sendUrl) {
        this.message = message;
        this.messageFlag = messageFlag;
        this.sendUrl = sendUrl;
    }

    public boolean isNoticeMsg() {
        return this.messageFlag==MESSAGE_NOTICE?true:false;
    }
    public boolean isExceptionMsg() {
        return this.messageFlag==MESSAGE_EXCEPTION?true:false;
    }
}
