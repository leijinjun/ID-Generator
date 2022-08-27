package com.lei2j.core.idgen;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Long类型Id分段对象
 * @author leijinjun
 * @date 2021/10/7
 **/
public class IDSegment implements ID {

    /**
     * inclusive
     */
    private long minId;

    /**
     * exclusive
     */
    private long maxId;

    private final AtomicLong curId;

    /**
     * 构造一个顺序增长的Id分段存储器
     * @param minId 最小id（包含）
     * @param maxId 最大id（不包含）
     */
    public IDSegment(long minId, long maxId) {
        this.minId = minId;
        this.maxId = maxId;
        this.curId = new AtomicLong(minId);
    }

    @Override
    public Long getId() {
        if (curId.get() >= maxId) {
            return null;
        }
        long increment = curId.getAndIncrement();
        return increment < maxId ? increment : null;
    }

    @Override
    public int capacity() {
        return (int) (maxId - minId);
    }

    @Override
    public int remain() {
        return (int) (maxId - curId.get());
    }

    public long getMinId() {
        return minId;
    }

    public void setMinId(Long minId) {
        this.minId = minId;
    }

    public long getMaxId() {
        return maxId;
    }

    public void setMaxId(Long maxId) {
        this.maxId = maxId;
    }
}
