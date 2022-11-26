package com.lei2j.idgen.core.snowflake.clock;

/**
 * 基于初始化时间累加
 * @author leijinjun
 * @date 2022/11/10
 **/
public class AccumulateHistoricalClock implements Clock{

    private long lastTimeStamp = java.time.Clock.systemUTC().millis();

    @Override
    public long now() {
        return lastTimeStamp();
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
        return lastTimeStamp + 1;
    }
}
