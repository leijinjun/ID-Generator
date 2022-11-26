package com.lei2j.idgen.core.snowflake;

/**
 * 雪花算法配置项
 * @author leijinjun
 * @date 2022/2/8
 **/
public class SnowFlakeConfig {

    /**
     *  默认应用id，用于单体应用程序的默认机器id
     */
    public final static long DEFAULT_WORK_ID = 1;
    /**
     * 时间戳bit位数
     */
    private long timestampBits = 41L;

    /**
     * 机器ID bit位数
     */
    private long workerIdBits = 10L;

    /**
     * 序列号 bit位数
     */
    private long sequenceBits = 12L;

    /**
     * 节点id
     */
    private long workId = DEFAULT_WORK_ID;

    public SnowFlakeConfig() {
    }

    public SnowFlakeConfig(long timestampBits, long workerIdBits, long sequenceBits, long workId) {
        this.timestampBits = timestampBits;
        this.workerIdBits = workerIdBits;
        this.sequenceBits = sequenceBits;
        this.workId = workId;
    }

    public long getTimeBits() {
        return timestampBits;
    }

    public long getWorkerBits() {
        return workerIdBits;
    }

    public long getSequenceBits() {
        return sequenceBits;
    }

    public long maxWorkId() {
        return ~(-1L << workerIdBits);
    }

    public long getWorkId() {
        return workId;
    }
}
