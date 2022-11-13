package com.lei2j.idgen.core.snowflake;

/**
 * @author leijinjun
 * @date 2021/11/8
 **/
public class DefaultSnowFlakeConfig implements SnowFlakeConfig {
    /**
     *  默认应用id，用于单体应用程序的默认机器id
     */
    public final static long DEFAULT_WORK_ID = 1;
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

    @Override
    public long maxWorkId() {
        return ~(-1L << workerIdBits);
    }

    @Override
    public long getWorkId() {
        return DEFAULT_WORK_ID;
    }
}
