package com.lei2j.idgen;

import com.lei2j.core.idgen.SerialNo;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author leijinjun
 * @date 2021/10/7
 **/
public class IDSegment implements SerialNo {

    /**
     * inclusive
     */
    private Long minId;

    /**
     * exclusive
     */
    private Long maxId;

    private AtomicLong curId;

    @Override
    public Long getSerialNo() {
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

    public IDSegment(Long minId, Long maxId) {
        this.minId = minId;
        this.maxId = maxId;
        this.curId = new AtomicLong(minId);
    }

    public Long getMinId() {
        return minId;
    }

    public void setMinId(Long minId) {
        this.minId = minId;
    }

    public Long getMaxId() {
        return maxId;
    }

    public void setMaxId(Long maxId) {
        this.maxId = maxId;
    }

    public AtomicLong getCurId() {
        return curId;
    }

    public void setCurId(AtomicLong curId) {
        this.curId = curId;
    }
}
