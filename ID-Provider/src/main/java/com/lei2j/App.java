package com.lei2j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

/**
 *
 * @author lei2j
 */
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Autowired
    private IdGenService idGenService;

    @PostConstruct
    private void test() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CopyOnWriteArraySet<Long> copyOnWriteArraySet = new CopyOnWriteArraySet<>();
        int count = 200000;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        long st = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            executorService.execute(()->{
                try {
                    Long testA = idGenService.getId("testA");
                    System.out.println(testA);
                    if (testA != null) {
                        copyOnWriteArraySet.add(testA);
                    }
                }finally {
                    countDownLatch.countDown();

                }
            });
        }
        countDownLatch.await();
        long ed = System.currentTimeMillis();
        System.out.println("Time:" + (ed - st));
        System.out.println("size="+copyOnWriteArraySet.size());
        System.out.println("QPS=" + (copyOnWriteArraySet.size() * 1000 / (ed - st)));
        System.exit(0);
    }
}
