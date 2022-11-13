package com.lei2j.idgen.core.segment;

import com.lei2j.idgen.core.IdGenerator;

/**
 *  抽象id片段生成器
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
