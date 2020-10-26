package com.enliple.ar.worker;

import com.enliple.ar.jpa.db1.service.SupplementService;
import com.enliple.ar.jpa.db2.domain.JobResultAr;
import com.enliple.ar.jpa.db2.service.JobResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("AR_RESULT")
@Slf4j
public class RecomResultAr implements JobResultService {


    @Resource(name = "AR_RESULT_JPA")
    private JobResultService jobResultService;

    @Override
    public void insertArResult(JobResultAr jobResultAr) {
        jobResultService.insertArResult(jobResultAr);
    }
}
