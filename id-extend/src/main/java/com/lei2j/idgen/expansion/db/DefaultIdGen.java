package com.lei2j.idgen.expansion.db;

import com.lei2j.idgen.core.segment.BizType;
import com.lei2j.idgen.core.segment.IDResource;
import com.lei2j.idgen.core.segment.SegmentGenerator;
import com.lei2j.idgen.core.segment.SegmentIdBufferGenerator;
import com.lei2j.idgen.expansion.db.resource.IdSegmentResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认id生成服务
 * @author leijinjun
 * @date 2022/7/12
 **/
public class DefaultIdGen implements IdGen {

    private final Map<String, SegmentGenerator> segmentGeneratorMap = new HashMap<>();

    private final Map<String, IDResource> idResourceMap = new HashMap<>();

    private final IdSegmentResource resource;

    public DefaultIdGen(@Autowired IdSegmentResource resource) {
        this.resource = resource;
    }

    @Override
    public Object next(BizType bizType) {
        final String businessType = bizType.getBusinessType();
        if (!segmentGeneratorMap.containsKey(businessType)) {
            synchronized (this) {
                if (!segmentGeneratorMap.containsKey(businessType)) {
                    idResourceMap.putIfAbsent(businessType, () -> resource.getIdSegment(bizType));
                    IDResource idResource = idResourceMap.get(businessType);
                    segmentGeneratorMap.putIfAbsent(businessType, new SegmentIdBufferGenerator(idResource));
                }
            }
        }
        final SegmentGenerator segmentGenerator = segmentGeneratorMap.get(businessType);
        return segmentGenerator.next();
    }
}
