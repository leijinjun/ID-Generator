package com.lei2j.core.id.gen;

/**
 * @author leijinjun
 * @date 2021/10/4
 **/
public abstract class IDGenerator {

    protected IDResource idResource;

    IDGenerator(IDResource idResource) {
        this.idResource = idResource;
    }

    /**
     * 初始化缓存
     */
    public void initBuffer() {
    }

    /**
     * 获取一个全局唯一ID
     * @return
     */
    public abstract Object next();
}
