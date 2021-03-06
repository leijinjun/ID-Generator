package com.lei2j.core.idgen;

import com.lei2j.core.IdGenerator;

/**
 * @author leijinjun
 * @date 2021/10/4
 **/
public abstract class AbstractSegmentIdGenerator implements IdGenerator {

    protected IDResource idResource;

    AbstractSegmentIdGenerator(IDResource idResource) {
        this.idResource = idResource;
    }

    /**
     * 初始化缓存
     */
    protected abstract void initBuffer();

}
