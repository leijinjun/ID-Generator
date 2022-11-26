package com.lei2j.idgen.expansion.snowflake.autoconfig;

import com.lei2j.idgen.core.snowflake.SnowFlakeConfig;
import com.lei2j.idgen.expansion.snowflake.database.DatabaseWorkIdConfig;
import com.lei2j.idgen.expansion.snowflake.WorkIdConfig;
import com.lei2j.idgen.expansion.snowflake.database.DbNodeService;
import com.lei2j.idgen.expansion.snowflake.local.LocalWorkIdConfig;
import com.lei2j.idgen.expansion.snowflake.zookeeper.ZookeeperWorkIdConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author leijinjun
 * @date 2022/11/13
 **/
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({SnowflakeProperties.class})
@ConditionalOnClass({WorkIdConfig.class})
public class SnowflakeAutoConfiguration {

    @Autowired
    private SnowflakeProperties snowflakeProperties;

    @Bean
    @ConditionalOnMissingBean
    public JdbcTemplate jdbcTemplate(@Autowired DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "idgen.type")
    public WorkIdConfig workIdConfig() {
        if (snowflakeProperties.getType() == SnowflakeProperties.Type.db) {
            return new DatabaseWorkIdConfig();
        } else if (snowflakeProperties.getType() == SnowflakeProperties.Type.zk) {
            return new ZookeeperWorkIdConfig();
        } else {
            if (snowflakeProperties.getWorkId() == null) {
                throw new IllegalArgumentException("When idgen.type value is local,idgen.workId property is not configured");
            }
            return new LocalWorkIdConfig(snowflakeProperties.getWorkId());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "idgen.type",havingValue = "db")
    public DbNodeService dbNodeService(){
        return new DbNodeService();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "idgen.type", havingValue = "zk")
    public CuratorFramework curatorFramework() {
        return CuratorFrameworkFactory.builder().connectString(snowflakeProperties.getZkConnect())
                .retryPolicy(new RetryNTimes(3, 1000))
                .namespace(snowflakeProperties.getNamespace())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SnowFlakeConfig snowFlakeConfig(WorkIdConfig workIdConfig) throws Exception {
        return new SnowFlakeConfig(snowflakeProperties.getTimestampBits(),
                snowflakeProperties.getWorkerIdBits(),
                snowflakeProperties.getSequenceBits(),
                workIdConfig.initWorkId());
    }
}
