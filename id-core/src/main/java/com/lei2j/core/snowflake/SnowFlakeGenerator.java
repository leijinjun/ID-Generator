package com.lei2j.core.snowflake;

import com.lei2j.core.IdGenerator;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 生成id的可操作最大bits数
     */
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
     * 最后一次的时间戳减去起始时间戳(START_TIME)
     */
    private volatile long lastTimeStamp;

    /**
     * 序列号生成器
     */
    private final AtomicLong sequence = new AtomicLong(0);

    /**
     * 使用默认的配置项{@Link DefaultSnowFlakeConfig}
     */
    public SnowFlakeGenerator(){
        this(new DefaultSnowFlakeConfig());
    }

    /**
     * @param snowFlakeConfig 雪花算法配置项
     */
    public SnowFlakeGenerator(SnowFlakeConfig snowFlakeConfig){
        this(snowFlakeConfig, DEFAULT_WORK_ID);
    }

    /**
     * @param snowFlakeConfig 雪花算法配置项
     * @param workerId
     */
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
            throw new IllegalArgumentException("timestampBits + workerIdBits + sequenceBits great than " + MAX_BITS);
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
        final long workId = workerId;
        long sequenceId;
        long ts;
        while (true) {
            ts = getTimeStamp();
            this.lastTimeStamp = ts;
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
            try {
                writeLock.tryLock(20, TimeUnit.MILLISECONDS);
                try {
                    if (sequence.get() > maxSequenceId) {
                        sequence.set(0L);
                    }
                } finally {
                    writeLock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private long getTimeStamp(){
        long currentTime;
        for (int i = 0; true; i++) {
            if ((currentTime = getCurrentTime()) >= (this.lastTimeStamp + START_TIME)) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (i > 2) {
                throw new RuntimeException("时间出现回拨");
            }
        }
        long ts = currentTime - START_TIME;
        if (ts > maxTimeStamp) {
            throw new RuntimeException("时间戳超出限制");
        }
        return ts;
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
