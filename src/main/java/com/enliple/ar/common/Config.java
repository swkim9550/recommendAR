package com.enliple.ar.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {

    @Value("${redis.hosts}")
    @Getter @Setter private String[] redisHosts;

    @Value("${mysql.driver}")
    @Getter private String mysqlDriver;
    @Value("${mysql.url}")
    @Getter private String mysqlUrl;
    @Value("${mysql.user}")
    @Getter private String mysqlUser;
    @Value("${mysql.password}")
    @Getter private String mysqlPassword;

    @Value("${mysql.url.for.joblist}")
    @Getter private String mysqlUrlForJoblist;
    @Value("${mysql.user.for.joblist}")
    @Getter private String mysqlUserForJoblist;
    @Value("${mysql.password.for.joblist}")
    @Getter private String mysqlPasswordForJoblist;
    @Value("${redise.expire.time}")
    @Getter private int redisExpireTime;

    @Value("${telegram.cfengine.local.server}")
    @Getter private String telegramServer = "127.0.0.1:26000";
    @Value("${telegram.cfengine.notice.resturl}")
    @Getter private String telegramSendUrlNotice = "http://127.0.0.1:8080/bot_cfengine";
    @Value("${telegram.cfengine.exception.resturl}")
    @Getter private String telegramSendUrlException = "http://127.0.0.1:8080/bot_cfengine_exception";
    @Value("${telegram.cfengine.send}")
    @Getter private boolean telegramSend = false;

    @Value("${recom.file.path}")
    @Getter private String recomFilePath;

    @Value("${recom.logread}")
    @Getter String fileLog;
}
