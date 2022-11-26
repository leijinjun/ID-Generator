package com.lei2j.idgen.expansion.db;

import com.lei2j.idgen.core.segment.BizType;

/**
 * id生成服务
 * @author leijinjun
 * @date 2022/11/14
 **/
public interface IdGen {

    /**
     * 返回id
     * @param bizType
     * @return
     */
    Object next(BizType bizType);
}
