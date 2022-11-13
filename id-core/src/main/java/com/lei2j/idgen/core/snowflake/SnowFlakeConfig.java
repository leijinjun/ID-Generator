package com.lei2j.idgen.core.snowflake;

/**
 * 雪花算法配置项
 * @author leijinjun
 * @date 2022/2/8
 **/
public interface SnowFlakeConfig {

    /**
     * 获取时间戳 bit位数
     * @return
     */
    long getTimeBits();

    /**
     * 获取机器ID bit位数
     * @return
     */
    long getWorkerBits();

    /**
     * 获取序列号bit位数
     * @return
     */
    long getSequenceBits();

    /**
     * 根据{@link #getWorkerBits()}，返回最大节点id
     * @return 最大节点id
     */
    long maxWorkId();

    /**
     * 返回一个节点id
     * @return 节点id
     */
    long getWorkId();
}
