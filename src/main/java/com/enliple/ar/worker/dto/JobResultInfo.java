package com.enliple.ar.worker.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class JobResultInfo {
    private int dateTime;

    private String advId;

    private boolean pc;

    private long pcodeCount;

    private long auidCount;

    private LocalDateTime createTime;

    @Builder
    public JobResultInfo(int dateTime, String advId, boolean pc, long pcodeCount, long auidCount, LocalDateTime createTime) {
        this.dateTime = dateTime;
        this.advId = advId;
        this.pc = pc;
        this.pcodeCount = pcodeCount;
        this.auidCount = auidCount;
        this.createTime = createTime;
    }
}
