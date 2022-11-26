package com.lei2j.idgen.expansion.snowflake.local;

import com.lei2j.idgen.expansion.snowflake.WorkIdConfig;

/**
 * 根据本地配置文件获取节点id
 * @author leijinjun
 * @date 2022/11/12
 **/
public class LocalWorkIdConfig implements WorkIdConfig {

    private final long workId;

    public LocalWorkIdConfig(long workId) {
        if (workId < 0) {
            throw new IllegalArgumentException("workId < 0");
        }
        this.workId = workId;
    }

    @Override
    public long initWorkId() {
        return workId;
    }

    @Override
    public void keepLive() {
        //do nothing
    }

}
