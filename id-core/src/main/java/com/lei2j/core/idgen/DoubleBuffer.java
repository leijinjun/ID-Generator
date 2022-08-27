package com.lei2j.core.idgen;

/**
 * 双Buffer缓存结构接口
 * @author leijinjun
 * @date 2021/10/18
 **/
public interface DoubleBuffer {

    /**
     * 添加一个号段到Buffer中
     * @param serialNo
     */
    void addSegment(SerialNo serialNo);

    /**
     * 切换
     */
    void switchBuffer();

    /**
     * 判断是否需要补充备用Buffer
     * @param proportion
     * @return
     */
    boolean ifSupplementNextBuffer(int proportion);

    /**
     * 判断当前Buffer是否还有库存，返回{@code true}表示还有剩余。
     *
     * @return
     */
    boolean hasRemaining();

    /**
     * 获取备用Buffer,可能为空
     * @return
     */
    SerialNo nextBuffer();

    /**
     * 获取一个唯一Id
     * @return
     */
    Object getId();
}
