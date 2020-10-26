package com.enliple.ar.worker;

import com.enliple.ar.worker.dto.JobResultInfo;

public interface RecomMaker {
    //추천데이터 생성 결과를 저장하는 메소드
    public void insertRecomResult(JobResultInfo jobResultinfo, long resultCount);
}
