package com.lei2j.idgen.core.snowflake.clock;

import java.util.concurrent.TimeUnit;

/**
 * 本地时钟源
 * @author leijinjun
 * @date 2022/11/9
 **/
public class LocalClock implements Clock {

    /**
     * 该时钟最后一次的时间戳
     */
    private long lastTimeStamp;

    @Override
    public long now() {
        return java.time.Clock.systemUTC().millis();
    }

    @Override
    public long lastTimeStamp() {
        return lastTimeStamp;
    }

    @Override
    public void updateLastTimeStamp(long lastTimeStamp) {
        this.lastTimeStamp = Math.max(this.lastTimeStamp, lastTimeStamp);
    }

    @Override
    public long nextTime(long nextTick) {
        int n = 0;
        int maxRetryTimes = 2;
        while (nextTick <= this.lastTimeStamp && n <maxRetryTimes) {
            final long l = this.lastTimeStamp - nextTick;
            if (l > 5 && l <= 1000 * 2) {
                try {
                    TimeUnit.MILLISECONDS.sleep(l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                n++;
            } else if (l <= 5) {
                try {
                    TimeUnit.MILLISECONDS.sleep(Math.max(l, 1) << 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            nextTick = now();
            n++;
        }
        return nextTick;
    }

}
