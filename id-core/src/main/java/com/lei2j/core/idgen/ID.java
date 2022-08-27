package com.lei2j.core.idgen;

/**
 * Id对象
 * @author leijinjun
 * @date 2021/10/4
 **/
public interface ID {

    /**
     * 获取唯一一个序列号
     * @return 返回一个唯一id，
     */
    Object getId();

    /**
     * 获取id片段容量
     * @return
     */
    int capacity();

    /**
     * 获取剩余可用id数量
     * @return
     */
    int remain();
}
