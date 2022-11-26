package com.lei2j.idgen.core.segment;

/**
 *  业务数据类型，可在此类上进行扩展。<br/>
 *  如添加前缀，后缀等
 * @author leijinjun
 * @date 2022/11/13
 **/
public class BizType {

    private String businessType;

    public BizType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    @Override
    public String toString() {
        return "BizType{" +
                "businessType='" + businessType + '\'' +
                '}';
    }
}
