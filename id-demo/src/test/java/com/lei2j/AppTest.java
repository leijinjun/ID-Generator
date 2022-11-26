package com.lei2j;

import static org.junit.Assert.assertTrue;

import com.lei2j.idgen.core.IdGenerator;
import com.lei2j.idgen.core.snowflake.SnowFlakeGenerator;
import com.lei2j.idgen.core.snowflake.clock.AccumulateHistoricalClock;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void snowFlakeTest(){
        final SnowFlakeGenerator snowFlakeGenerator = new SnowFlakeGenerator();
        int size = 500000;
        final HashSet<Object> hashSet = new HashSet<>(size);
        long st = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            hashSet.add(snowFlakeGenerator.next());
        }
        long ed = System.currentTimeMillis();
        System.out.println("time:" + (ed - st) + "ms");
        System.out.println(hashSet.size());
    }

    @Test
    public void snowFlakeIdTest() throws InterruptedException {
        Set<Object> set = new CopyOnWriteArraySet<>();
        int length = 100000;
        final ExecutorService executorService = Executors.newFixedThreadPool(100);
        final IdGenerator snowFlakeGenerator = new SnowFlakeGenerator(new AccumulateHistoricalClock());
        CountDownLatch latch = new CountDownLatch(length);
        final long l1 = System.currentTimeMillis();
        for (int i = 0; i < length; i++) {
            executorService.execute(()->{
                final Object next;
                try {
                    next = snowFlakeGenerator.next();
                    System.out.println(Thread.currentThread().getName() + "-" + next);
                    if (!set.add(next)) {
                        executorService.shutdownNow();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        Assert.isTrue(set.size() == length);
        System.out.println("=================:" + set.size());
        System.out.println("=================end");
    }
}
