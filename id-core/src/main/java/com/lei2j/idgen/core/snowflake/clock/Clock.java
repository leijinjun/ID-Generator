package com.lei2j.idgen.core.snowflake.clock;

/**
 *  雪花算法-时钟
 * @author leijinjun
 * @date 2022/11/9
 **/
public interface Clock {

    /**
     * 返回时钟最新时间戳
     * @return
     */
    long now();

    /**
     * 返回最后一次的时间戳，也就是当前雪花算法的最后使用的时间戳
     * @return 最后一次的时间戳
     */
    long lastTimeStamp();

    /**
     * 更新雪花算法最后的时间戳
     * @param lastTimeStamp 雪花算法最后使用的时间戳
     */
    void updateLastTimeStamp(long lastTimeStamp);

    /**
     * 返回一个比当前时间戳大的时间戳
     * @param nextTick 从{{@link #now()}}获取的一个时间戳
     * @return
     */
    long nextTime(long nextTick);
}
