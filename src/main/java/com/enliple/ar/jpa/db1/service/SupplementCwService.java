package com.enliple.ar.jpa.db1.service;

import com.enliple.ar.dao.RedisCluster;
import com.enliple.ar.jpa.db1.domain.*;
import com.enliple.ar.jpa.db1.dto.CartArCountDto;

import java.util.List;

public interface SupplementCwService {
    List<TotalCartAdverId> getTotalCartAdverIdList();

    CartArCountDto saveRedisSupplement(String adverId, String purchaseType);
}
