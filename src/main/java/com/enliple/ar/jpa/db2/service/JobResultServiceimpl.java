package com.enliple.ar.jpa.db2.service;

import com.enliple.ar.jpa.db2.domain.JobResultAr;
import com.enliple.ar.jpa.db2.repository.JobResultArRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("AR_RESULT_JPA")
public class JobResultServiceimpl implements JobResultService {

    @Autowired
    private JobResultArRepository jobResultArRepository;

    @Override
    public void insertArResult(JobResultAr jobResultAr) {
        try{
            jobResultArRepository.save(jobResultAr);
        }catch (Exception e){
            log.info(e.toString());
        }
    }
}
