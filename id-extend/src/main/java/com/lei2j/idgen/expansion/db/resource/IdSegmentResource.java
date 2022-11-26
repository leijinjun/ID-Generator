package com.lei2j.idgen.expansion.db.resource;

import com.lei2j.idgen.core.segment.BizType;
import com.lei2j.idgen.core.segment.ID;

/**
 * id资源器服务
 * @author leijinjun
 * @date 2022/11/14
 **/
public interface IdSegmentResource {

    /**
     * 返回id资源
     * @param bizType
     * @return
     */
    ID getIdSegment(BizType bizType);
}
