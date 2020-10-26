package com.enliple.ar.jpa.db1.service;

import com.enliple.ar.jpa.db1.domain.*;
import com.enliple.ar.jpa.db1.dto.SupplementDto;
import com.enliple.ar.jpa.db1.key.ArKey;
import com.enliple.ar.jpa.db2.domain.JobResultAr;
import com.enliple.ar.worker.dto.JobResultInfo;

import java.util.List;

public interface SupplementService {
    List<TotalSameOrder> getTotalSameOrders();

    List<TotalContinuityOrder> getTotalContinuityOrders();

    /**
     * <pre>
     *   기능 명 : 동시구매
     * </pre>
     * @param adverId
     * @return
     */
    List<SameOrder> getSameOrders(String adverId);

    /**
     * <pre>
     *   기능 명 : 연속구매
     * </pre>
     * @param adverId
     * @return
     */
    List<ContinuityOrder> getContinuityOrder(String adverId);

    int saveSupplement(SupplementDto dto);

    List<SameOrder> readLogFile();
}
