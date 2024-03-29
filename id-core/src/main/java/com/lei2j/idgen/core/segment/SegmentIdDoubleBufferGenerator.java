package com.lei2j.idgen.core.segment;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 双缓存Buffer id片段
 * @author leijinjun
 * @date 2021/10/9
 **/
public class SegmentIdDoubleBufferGenerator extends AbstractSegmentIdGenerator {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(false);

    private final ScheduledExecutorService doubleBufferExecuteService = new ScheduledThreadPoolExecutor(1,
            new ThreadFactory() {
                private final AtomicInteger incr = new AtomicInteger(0);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "ID-SEGMENT-" + incr.getAndIncrement());
                }
            });

    private final DoubleBuffer doubleBuffer;

    /**
     * 1：库存充足
     * 2: 待补充
     * 3：补充完成
     */
    private final AtomicInteger suppleState = new AtomicInteger();

    /**
     * 充足
     */
    private final static int ENOUGH = 0x1;

    /**
     * 等待补充
     */
    private final static int WAIT_SUPPLE = 0x2;

    /**
     * 已补充
     */
    private final static int ADDED = 0x3;

    private final static int DEFAULT_WARNING_VAL = 30;

    /**
     * 当前缓存已使用量百分比
     */
    private final int earlyWarningValue;

    /**
     * buffer是否已初始化
     */
    private volatile boolean init;

    public SegmentIdDoubleBufferGenerator(IDResource idResource) {
        this(idResource, new LinkedBuffer());
//        this(idResource, new ArrayBuffer());
    }

    public SegmentIdDoubleBufferGenerator(IDResource idResource, DoubleBuffer doubleBuffer) {
        this(idResource, doubleBuffer, DEFAULT_WARNING_VAL);
    }

    public SegmentIdDoubleBufferGenerator(IDResource idResource, DoubleBuffer doubleBuffer, int earlyWarningValue) {
        super(idResource);
        this.doubleBuffer = doubleBuffer;
        this.earlyWarningValue = earlyWarningValue;
        initBuffer();
    }

    @Override
    public void initBuffer() {
        assert !init;
        doubleBuffer.addSegment(Objects.requireNonNull(idResource.get(), "idSegment is null"));
        doubleBuffer.switchBuffer();
        suppleState.set(ENOUGH);
        init = true;
    }

    @Override
    public Object next() {
        Object id;
        while (true) {
            lock.readLock().lock();
            try {
                id = doubleBuffer.getId();
                if (doubleBuffer.ifSupplementNextBuffer(earlyWarningValue)) {
                    if (suppleState.compareAndSet(ENOUGH, WAIT_SUPPLE)) {
//                doubleBufferExecuteService.execute(this::supply);
                        doubleBufferExecuteService.schedule(this::supplement, (new SecureRandom().nextInt(4) + 1) * 10
                                , TimeUnit.MILLISECONDS);
                    }
                }
                if (id != null) {
                    return id;
                }
            } finally {
                lock.readLock().unlock();
            }
            try {
                if (lock.writeLock().tryLock(50, TimeUnit.MILLISECONDS)) {
                    try {
                        //如果第二Buffer还未补充
                        if (suppleState.get() == WAIT_SUPPLE) {
                            supplement();
                        }
                        //当前Buffer无缓存号段并且下一Buffer已补充完毕
                        if (!doubleBuffer.hasRemaining() && suppleState.compareAndSet(ADDED, ENOUGH)) {
                            doubleBuffer.switchBuffer();
                        }
                    } finally {
                        lock.writeLock().unlock();
                    }

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 补充缓存
     */
    private void supplement() {
        lock.writeLock().lock();
        try {
            if (suppleState.get() != WAIT_SUPPLE) {
                return;
            }
            ID next = doubleBuffer.nextBuffer();
            if (next == null || next.remain() <= 0) {
                doubleBuffer.addSegment(idResource.get());
            }
            //补充完毕
            suppleState.compareAndSet(WAIT_SUPPLE, ADDED);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 数组实现双Buffer缓存
     */
    private static class ArrayBuffer implements DoubleBuffer {

        private final ID[] arrBuf = new ID[2];

        private volatile int currentPos = -1;

        @Override
        public void addSegment(ID ID) {
            if (currentPos < 0) {
                arrBuf[0] = ID;
            }
            arrBuf[getNextPos()] = ID;
        }

        @Override
        public void switchBuffer() {
            if (currentPos < 0) {
                currentPos = 0;
            } else {
                currentPos = getNextPos();
            }
        }

        @Override
        public boolean ifSupplementNextBuffer(int proportion) {
            int capacity = arrBuf[currentPos].capacity();
            return capacity * proportion / 100 < (capacity - arrBuf[currentPos].remain());
        }

        @Override
        public boolean hasRemaining() {
            return arrBuf[currentPos] != null && arrBuf[currentPos] != null && arrBuf[currentPos].remain() > 0;
        }

        @Override
        public ID nextBuffer() {
            return arrBuf[getNextPos()];
        }

        @Override
        public Object getId() {
            return arrBuf[currentPos].getId();
        }

        private int getNextPos() {
            return (currentPos + 1) % 2;
        }
    }

    /**
     * 链表实现双Buffer缓存
     */
    private static class LinkedBuffer implements DoubleBuffer {

        private final LinkedBuffer head = new LinkedBuffer();

        private LinkedBuffer next = new LinkedBuffer();

        private LinkedBuffer current;

        private ID idSegment;

        private int currentSegmentCapacity;

        public LinkedBuffer() {
            LinkedBuffer tail = new LinkedBuffer();
            this.head.next = tail;
            tail.next = this.head;
        }

        @Override
        public void addSegment(ID ID){
            if (this.current == null) {
                this.head.idSegment = ID;
            } else {
                this.current.next.idSegment = ID;
            }
        }

        @Override
        public void switchBuffer() {
            if (current == null) {
                current = head;
            } else {
                this.current = this.current.next;
            }
            currentSegmentCapacity = current.idSegment.capacity();
        }

        @Override
        public boolean ifSupplementNextBuffer(int proportion) {
            return currentSegmentCapacity * proportion / 100 < (currentSegmentCapacity-current.idSegment.remain());
        }

        @Override
        public boolean hasRemaining() {
            return current != null && current.idSegment != null && current.idSegment.remain() > 0;
        }

        @Override
        public ID nextBuffer() {
            return current != null && current.next != null ? current.next.idSegment : null;
        }

        @Override
        public Object getId(){
            return current.idSegment.getId();
        }
    }
}
