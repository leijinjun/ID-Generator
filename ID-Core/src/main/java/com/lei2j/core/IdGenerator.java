package com.lei2j.core;

/**
 *  唯一id生成器
 * @author leijinjun
 * @date 2022/2/8
 **/
public interface IdGenerator {

    /**
     * 获取一个全局唯一ID
     * @return
     */
    Object next();

}
