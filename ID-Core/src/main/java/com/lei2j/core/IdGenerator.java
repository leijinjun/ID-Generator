package com.lei2j.core;

/**
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
