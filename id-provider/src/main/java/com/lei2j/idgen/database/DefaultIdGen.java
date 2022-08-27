package com.lei2j.idgen.database;

import com.lei2j.core.idgen.IDResource;
import com.lei2j.core.idgen.SegmentIdBufferGenerator;
import com.lei2j.idgen.database.resource.DefaultIdSegmentResource;
import org.springframework.stereotype.Component;

/**
 * @author leijinjun
 * @date 2022/7/12
 **/
@Component
public class DefaultIdGen {

    private final DefaultIdSegmentResource provider;

    public DefaultIdGen(DefaultIdSegmentResource provider) {
        this.provider = provider;
    }

    public Object next(String businessType) {
        final IDResource supplier = () -> provider.getIdSegment(businessType);
        final SegmentIdBufferGenerator generator = new SegmentIdBufferGenerator(supplier);
        return generator.next();
    }
}
