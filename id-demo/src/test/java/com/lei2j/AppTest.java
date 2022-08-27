package com.lei2j;

import static org.junit.Assert.assertTrue;

import com.lei2j.core.IdGenerator;
import com.lei2j.core.snowflake.SnowFlakeGenerator;
import org.junit.Test;

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
    public void snowFlakeIdTest() throws InterruptedException {
        Set<Long> set = new CopyOnWriteArraySet<>();
        final ExecutorService executorService = Executors.newFixedThreadPool(100);
        final IdGenerator snowFlakeGenerator = new SnowFlakeGenerator();
        CountDownLatch latch = new CountDownLatch(500000);
        final long l1 = System.currentTimeMillis();
        for (int i = 0; i < 500000; i++) {
            executorService.execute(()->{
                final Long next = (Long) snowFlakeGenerator.next();
                set.add(next);
//                System.out.println(next);
                latch.countDown();
            });
        }
        latch.await();
        System.out.println(set.size());
        final long l2 = System.currentTimeMillis();
        System.out.println(">>>>>>>>>>>" + (l2 - l1));
    }
}
