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

    private final DefaultIdSegmentResource resource;

    public DefaultIdGen(DefaultIdSegmentResource resource) {
        this.resource = resource;
    }

    public Object next(String businessType) {
        final IDResource idResource = () -> resource.getIdSegment(businessType);
        final SegmentIdBufferGenerator generator = new SegmentIdBufferGenerator(idResource);
        return generator.next();
    }
}
