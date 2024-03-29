package com.lei2j.idgen.core.segment;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 单Buffer缓存id片段
 * @author leijinjun
 **/
public class SegmentIdBufferGenerator extends AbstractSegmentIdGenerator {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    /**
     * ID缓存区
     */
    private volatile ID idSegment;

    /**
     * buffer是否已初始化
     */
    private volatile boolean init;

    public SegmentIdBufferGenerator(IDResource idResource) {
        super(idResource);
        initBuffer();
    }

    @Override
    public void initBuffer() {
        if (init) {
            throw new IllegalStateException("Initialized");
        }
        supplement();
        init = true;
    }

    /**
     * 补充缓存
     */
    private void supplement(){
        idSegment = idResource.get();
    }

    @Override
    public Object next() {
        while (true) {
            lock.readLock().lock();
            Object serialNo;
            try {
                serialNo = idSegment.getId();
                if (serialNo != null) {
                    return serialNo;
                }
            } finally {
                lock.readLock().unlock();
            }
            try {
                if (lock.writeLock().tryLock(50, TimeUnit.MILLISECONDS)) {
                    try {
                        serialNo = idSegment.getId();
                        if (serialNo != null) {
                            return serialNo;
                        }
                        supplement();
                    } finally {
                        lock.writeLock().unlock();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
