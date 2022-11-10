package com.lei2j.core.snowflake;

import com.lei2j.core.IdGenerator;
import com.lei2j.core.snowflake.clock.Clock;
import com.lei2j.core.snowflake.clock.LocalClock;

import java.util.Objects;

/**
 * SnowFlake算法ID生成器，实例线程安全
 * @author leijinjun
 * @date 2021/11/8
 **/
public class SnowFlakeGenerator implements IdGenerator {

    /**
     *  默认应用id，用于单体应用程序的默认机器id
     */
    public final static long DEFAULT_WORK_ID = 1;

    /**
     * 生成id的可操作最大bits数
     */
    private static final long MAX_BITS = 63;

    /**
     * 起始时间戳2021-05-24 00:00:00
     */
    private static final long START_TIME = 1631203200000L;

    /**
     * 序列号最大值
     */
    private final long maxSequenceId;

    /**
     * 时间戳左移位数
     */
    private final long timeStampShiftLeft;

    /**
     * 本机服务ID左移位数
     */
    private final long workerIdShiftLeft;

    /**
     * 本机服务ID
     */
    private final long workerId;

    /**
     * 当前序列化
     */
    private long sequenceId = 0;

    private final Clock clock;

    /**
     * 使用默认的配置项{@Link DefaultSnowFlakeConfig}
     */
    public SnowFlakeGenerator(){
        this(new DefaultSnowFlakeConfig(),new LocalClock());
    }

    public SnowFlakeGenerator(Clock clock) {
        this(new DefaultSnowFlakeConfig(), clock);
    }

    /**
     *  使用默认节点id{@code DEFAULT_WORK_ID}，作为本节点id
     * @param snowFlakeConfig 雪花算法配置项
     */
    public SnowFlakeGenerator(SnowFlakeConfig snowFlakeConfig, Clock clock) {
        this(snowFlakeConfig, clock, DEFAULT_WORK_ID);
    }

    /**
     * @param clock 时钟源
     * @param snowFlakeConfig 雪花算法配置项
     * @param workerId
     */
    public SnowFlakeGenerator(SnowFlakeConfig snowFlakeConfig, Clock clock, long workerId) {
        Objects.requireNonNull(snowFlakeConfig, "snowFlakeConfig is null");
        this.clock = Objects.requireNonNull(clock, "clock is null");
        //时间戳bit位数
        long timestampBits;
        if ((timestampBits = snowFlakeConfig.getTimeBits()) <= 0) {
            throw new IllegalArgumentException(snowFlakeConfig.getTimeBits() + "less than 0");
        }

        //本机服务ID bit位数
        long workerIdBits;
        if ((workerIdBits = snowFlakeConfig.getWorkerBits()) <= 0) {
            throw new IllegalArgumentException(snowFlakeConfig.getWorkerBits() + "less than 0");
        }

        //序列号bit位数
        long sequenceBits;
        if ((sequenceBits = snowFlakeConfig.getSequenceBits()) <= 0) {
            throw new IllegalArgumentException(snowFlakeConfig.getSequenceBits() + "less than 0");
        }
        if (timestampBits + workerIdBits + sequenceBits > MAX_BITS) {
            throw new IllegalArgumentException("timestampBits + workerIdBits + sequenceBits great than " + MAX_BITS);
        }
        if (Long.toBinaryString(this.workerId = workerId).length() > workerIdBits) {
            throw new IllegalArgumentException("workId too big");
        }
        //本机服务ID左移位数
        workerIdShiftLeft = sequenceBits;
        //时间戳左移位数
        timeStampShiftLeft = sequenceBits + workerIdBits;
        //本机服务ID最大值
        long maxWorkerId = ~(-1L << workerIdBits);
        //序列号最大值
        maxSequenceId = ~(-1L << sequenceBits);
        if (workerId > maxWorkerId) {
            throw new IllegalArgumentException("workId:" + workerId + " > max value:" + maxWorkerId);
        }
    }

    @Override
    public synchronized Object next() {
        long nextTick = getCurrentTime();
        final long lastTimeStamp = clock.lastTimeStamp();
        if (nextTick < lastTimeStamp) {
            nextTick = nextTime(nextTick);
        }else if (nextTick == lastTimeStamp){
            if ((++sequenceId) > maxSequenceId) {
                nextTick = nextTime(nextTick);
                sequenceId = 0L;
            }
        }else {
            sequenceId = 0L;
        }
        if (nextTick < clock.lastTimeStamp()) {
            throw new RuntimeException("时钟回拨");
        }
        clock.updateLastTimeStamp(nextTick);
        return ((nextTick - START_TIME) << timeStampShiftLeft) | (workerId << workerIdShiftLeft) | sequenceId;
    }

    /**
     * 获取当前时间的时间戳
     * @return
     */
    private long getCurrentTime(){
        return clock.now();
    }

    /**
     * 返回一个比当前时间戳大的时间戳
     * @param nextTick 从时钟类{@code Clock}获取的最新时间戳
     * @return
     */
    private long nextTime(long nextTick) {
        return clock.nextTime(nextTick);
    }
}
