package com.lei2j.idgen.expansion.snowflake.autoconfig;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Id-generator雪花算法配置属性
 *
 * @author leijinjun
 * @date 2022/11/13
 **/
@ConfigurationProperties(prefix = "idgen")
public class SnowflakeProperties implements InitializingBean {

    /**
     * workId来源
     */
    private Type type = Type.local;

    /**
     * 当{@code type=Type.local}时，设置本服务的节点id，
     * 集群内节点id必须各不相同，适用于小规模应用集群。
     */
    private Long workId;

    /**
     * 节点命名空间
     */
    private String namespace = "default";

    /**
     * 最大断联时间
     */
    private Duration keepAliveTimeout = Duration.ofHours(24);

    /**
     * 时间戳bit位数
     */
    private long timestampBits = 41;

    /**
     * 机器ID bit位数
     */
    private long workerIdBits = 10;

    /**
     * 序列号 bit位数
     */
    private long sequenceBits = 12;

    /**
     * zookeeper server ip:port,多个使用逗号连接
     * ip:host
     */
    private String zkConnect = "localhost:2181";

    /**
     * 集群内节点自身ip:port
     */
    private String selfHost;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getWorkId() {
        return workId;
    }

    public void setWorkId(Long workId) {
        this.workId = workId;
    }

    public String getZkConnect() {
        return zkConnect;
    }

    public void setZkConnect(String zkConnect) {
        this.zkConnect = zkConnect;
    }

    public String getSelfHost() {
        return selfHost;
    }

    public void setSelfHost(String selfHost) {
        this.selfHost = selfHost;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public long getTimestampBits() {
        return timestampBits;
    }

    public void setTimestampBits(long timestampBits) {
        this.timestampBits = timestampBits;
    }

    public long getWorkerIdBits() {
        return workerIdBits;
    }

    public void setWorkerIdBits(long workerIdBits) {
        this.workerIdBits = workerIdBits;
    }

    public long getSequenceBits() {
        return sequenceBits;
    }

    public void setSequenceBits(long sequenceBits) {
        this.sequenceBits = sequenceBits;
    }

    public Duration getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public void setKeepAliveTimeout(Duration keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
    }


    public long maxWorkId() {
        return ~(-1L << workerIdBits);
    }

    public enum Type {

        /**
         * 本地配置文件
         */
        local,
        /**
         * 数据库
         */
        db,
        /**
         * zookeeper
         */
        zk,
        ;
    }
}
