package com.enliple.ar.jpa.db2.domain;

import com.enliple.ar.jpa.db2.key.JobResultArKey;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name="job_list_ar")
public class JobResultAr implements Serializable {

    @EmbeddedId
    private JobResultArKey key;

    @Column(name="status")
    private String status;

    @Column(name="count")
    private long count;

    @Column(name="create_date")
    private LocalDateTime createTime;

    @Column(name="update_date")
    private LocalDateTime updateTime;

    public JobResultAr() {

    }

    @Builder
    public JobResultAr(JobResultArKey key, String status, long count, LocalDateTime createTime,LocalDateTime updateTime) {
        this.key = key;
        this.status = status;
        this.count = count;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}
