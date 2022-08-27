package com.lei2j.core.snowflake;

import com.lei2j.core.IdGenerator;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * SnowFlake算法ID生成器
 * @author leijinjun
 * @date 2021/11/8
 **/
public class SnowFlakeGenerator implements IdGenerator {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static final long MAX_BITS = 63;

    /**
     * 起始时间戳2021-05-24 00:00:00
     */
    private static final long START_TIME = 1631203200000L;

    /**
     * 时间戳最大值
     */
    private final long maxTimeStamp;

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
     * 最后一次的时间戳
     */
    private volatile long lastTimeStamp;

    /**
     * 序列号生成器
     */
    private final AtomicLong sequence = new AtomicLong(0);

    public SnowFlakeGenerator(SnowFlakeConfig snowFlakeConfig, long workerId) {
        Objects.requireNonNull(snowFlakeConfig, "snowFlakeConfig is null");

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
            throw new IllegalArgumentException("snowFlakeConfig is invalid");
        }
        if (Long.toBinaryString(this.workerId = workerId).length() > workerIdBits) {
            throw new IllegalArgumentException("workId too big");
        }
        //本机服务ID左移位数
        workerIdShiftLeft = sequenceBits;
        //时间戳左移位数
        timeStampShiftLeft = sequenceBits + workerIdBits;
        //时间戳最大值
        maxTimeStamp = ~(-1L << timestampBits);
        //本机服务ID最大值
        long maxWorkerId = ~(-1L << workerIdBits);
        //序列号最大值
        maxSequenceId = ~(-1L << sequenceBits);
        if (workerId > maxWorkerId) {
            throw new IllegalArgumentException("workId:" + workerId + " > max value:" + maxWorkerId);
        }
    }

    @Override
    public Object next() {
        long currentTime = getCurrentTime();
        final long lt = lastTimeStamp;
        if (currentTime < lt) {
            throw new RuntimeException("时间出现回拨");
        }
        lastTimeStamp = currentTime;
        long ts = currentTime - START_TIME;
        if (ts > maxTimeStamp) {
            throw new RuntimeException("时间戳超出限制");
        }
        final long workId = workerId;
        long sequenceId;
        while (true) {
            Lock readLock = readLock();
            readLock.lock();
            try {
                sequenceId = sequence.getAndIncrement();
                if (sequenceId <= maxSequenceId) {
                    break;
                }
            } finally {
                readLock.unlock();
            }
            resetSequence(sequenceId);
        }
        return (ts << timeStampShiftLeft) | (workId << workerIdShiftLeft) | sequenceId;
    }

    /**
     * 重置Sequence序列
     * @param currentValue 当前序列号
     */
    private void resetSequence(long currentValue) {
        if (sequence.get() > maxSequenceId) {
            Lock writeLock = writeLock();
            writeLock.lock();
            try {
                sequence.set(0L);
            } finally {
                writeLock.unlock();
            }
        }
    }

    /**
     * 获取当前时间的时间戳
     * @return
     */
    private long getCurrentTime(){
        return System.currentTimeMillis();
    }

    private Lock readLock(){
        return lock.readLock();
    }

    private Lock writeLock() {
        return lock.writeLock();
    }

    public static void main(String[] args) {
        DefaultSnowFlakeConfig snowFlakeConfig = new DefaultSnowFlakeConfig();
        SnowFlakeGenerator snowFlakeGenerator = new SnowFlakeGenerator(snowFlakeConfig, 255);
        snowFlakeGenerator.sequence.set(4095);
        System.out.println("ID:" + snowFlakeGenerator.next());
        System.out.println("ID:" + snowFlakeGenerator.next());
        System.out.println("ID:" + snowFlakeGenerator.next());
    }
}
