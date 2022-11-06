package com.lei2j;

import static org.junit.Assert.assertTrue;

import com.lei2j.core.IdGenerator;
import com.lei2j.core.snowflake.SnowFlakeGenerator;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Objects;
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
    public void snowFlakeIdTest() throws InterruptedException {
        Set<Object> set = new CopyOnWriteArraySet<>();
        int length = 100000;
        final ExecutorService executorService = Executors.newFixedThreadPool(100);
        final IdGenerator snowFlakeGenerator = new SnowFlakeGenerator();
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
