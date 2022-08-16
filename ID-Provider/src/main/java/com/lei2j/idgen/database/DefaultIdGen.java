package com.lei2j.idgen.database;

import com.lei2j.core.idgen.IDResource;
import com.lei2j.core.idgen.SegmentIdBufferGenerator;
import com.lei2j.idgen.database.provider.DefaultSegmentProvider;
import org.springframework.stereotype.Component;

/**
 * @author leijinjun
 * @date 2022/7/12
 **/
@Component
public class DefaultIdGen {

    private final DefaultSegmentProvider provider;

    public DefaultIdGen(DefaultSegmentProvider provider) {
        this.provider = provider;
    }

    public Object next(String businessType) {
        final IDResource supplier = () -> provider.getIdSegment(businessType);
        final SegmentIdBufferGenerator generator = new SegmentIdBufferGenerator(supplier);
        return generator.next();
    }
}
