package com.lei2j.core.idgen;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 单Buffer缓存号段
 * @author leijinjun
 **/
public class SegmentIdBufferGenerator extends AbstractSegmentIdGenerator {

//    private final ReentrantLock lock = new ReentrantLock(false);

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    /**
     * ID缓存区
     */
    private volatile SerialNo idSegment;

    public SegmentIdBufferGenerator(IDResource idResource) {
        super(idResource);
        initBuffer();
    }

    @Override
    public void initBuffer() {
        supplement();
    }

    /**
     * 补充缓存
     */
    private void supplement(){
        idSegment = idResource.getIdSegment();
    }

    @Override
    public Object next() {
        while (true) {
            lock.readLock().lock();
            Object serialNo;
            try {
                serialNo = idSegment.getSerialNo();
                if (serialNo != null) {
                    return serialNo;
                }
            } finally {
                lock.readLock().unlock();
            }
            try {
                if (lock.writeLock().tryLock(50, TimeUnit.MILLISECONDS)) {
                    try {
                        serialNo = idSegment.getSerialNo();
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
        /*Object serialNo;
        while ((serialNo = idSegment.getSerialNo()) == null) {
            try {
                if (lock.tryLock(50, TimeUnit.MILLISECONDS)) {
                    try {
                        if (idSegment.remain() <= 0) {
                            supplement();
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
        return serialNo;*/
    }
}
