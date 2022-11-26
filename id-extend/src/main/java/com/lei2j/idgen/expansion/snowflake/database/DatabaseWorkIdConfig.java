package com.lei2j.idgen.expansion.snowflake.database;

import com.lei2j.idgen.expansion.snowflake.WorkIdConfig;
import com.lei2j.idgen.expansion.snowflake.autoconfig.SnowflakeProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 采用数据库生成节点id
 * @author leijinjun
 * @date 2022/11/12
 **/
public class DatabaseWorkIdConfig implements WorkIdConfig, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseWorkIdConfig.class);
    /**
     * 节点id
     */
    private Long workId;

    /**
     * 节点命名空间
     */
    private String namespace;

    /**
     * 最大断联时间
     */
    private Duration keepAliveTimeout;

    /**
     * 心跳间隔
     */
    private final Duration heartbeatTime = Duration.ofSeconds(60);

    /**
     * 倍数
     */
    private int multiple = 0;

    /**
     * 持有者id
     */
    private final String locker;

    private ApplicationContext applicationContext;

    @Autowired
    private DbNodeService dbNodeService;

    private ScheduledExecutorService scheduledExecutorService;

    public DatabaseWorkIdConfig() {
        String prefix = "";
        try {
            prefix = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.locker = prefix + RandomStringUtils.randomNumeric(RandomUtils.nextInt(7, 14));
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public synchronized long initWorkId() {
        if (workId == null) {
            final SnowflakeProperties snowflakeProperties = applicationContext.getBean(SnowflakeProperties.class);
            final String namespace = snowflakeProperties.getNamespace();
            final long maxWorkId = snowflakeProperties.maxWorkId();
            final Duration keepAliveTimeout = snowflakeProperties.getKeepAliveTimeout();
            this.namespace = namespace;
            this.keepAliveTimeout = keepAliveTimeout;
            this.workId = dbNodeService.getWorkId(namespace, maxWorkId, locker, keepAliveTimeout);
            LOGGER.info("[WorkIdConfig]workId:{}", workId);
            if (scheduledExecutorService != null) {
                scheduledExecutorService.shutdown();
            }
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(this::keepLive, 60, heartbeatTime.getSeconds(), TimeUnit.SECONDS);
        }
        return workId;
    }

    @Override
    public void keepLive() {
        try {
            dbNodeService.keepLiveNode(namespace, workId, locker, keepAliveTimeout,
                    heartbeatTime.plusSeconds(heartbeatTime.getSeconds() * multiple));
            this.multiple = 0;
        } catch (Exception e) {
            if (this.multiple <= 3) {
                this.multiple++;
            }
            LOGGER.error("[DB.work.heartbeat]error:", e);
        }
    }

}
