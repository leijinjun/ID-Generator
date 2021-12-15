package com.lei2j;

/**
 * @author leijinjun
 * @date 2021/10/5
 **/
public interface IdGenService {

    /**
     * 获取ID
     * @param businessType
     * @return
     */
    Long getId(String businessType);
}
