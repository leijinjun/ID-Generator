package com.lei2j.idgen.expansion.snowflake;

/**
 * 节点id生成服务
 * @author leijinjun
 * @date 2022/11/12
 **/
public interface WorkIdConfig {

    /**
     * 初始化返回一个节点id
     * @return
     * @throws Exception
     */
    long initWorkId() throws Exception;

    /**
     * 节点保活
     */
    void keepLive();

}
