package com.lei2j.core.snowflake;

/**
 * @author leijinjun
 * @date 2021/11/8
 **/
public class DefaultSnowFlakeConfig implements SnowFlakeConfig {

    /**
     * 时间戳bit位数
     */
    private final long timestampBits = 41;

    /**
     * 机器ID bit位数
     */
    private final long workerIdBits = 10;

    /**
     * 序列号 bit位数
     */
    private final long sequenceBits = 12;

    @Override
    public long getTimeBits() {
        return timestampBits;
    }

    @Override
    public long getWorkerBits() {
        return workerIdBits;
    }

    @Override
    public long getSequenceBits() {
        return sequenceBits;
    }
}
