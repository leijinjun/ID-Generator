package com.lei2j.core.id.gen;

/**
 * @author leijinjun
 * @date 2021/10/4
 **/
public interface SerialNo {

    /**
     * 获取一个序列号
     * @return
     */
    Object getSerialNo();

    /**
     * 容量
     * @return
     */
    int capacity();

    /**
     * 剩余
     * @return
     */
    int remain();
}
