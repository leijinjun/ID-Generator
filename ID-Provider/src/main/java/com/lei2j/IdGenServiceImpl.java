package com.lei2j;

import com.lei2j.core.id.gen.IDBufferGenerator;
import com.lei2j.core.id.gen.IDDoubleBufferGenerator;
import com.lei2j.core.id.gen.IDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author leijinjun
 * @date 2021/10/5
 **/
@Service
public class IdGenServiceImpl implements IdGenService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Map<String,IDGenerator> idGeneratorMap = new HashMap<>();

    @Autowired
    IdGenSegmentServiceImpl idGenSegmentService;

    @PostConstruct
    private void setUp() {
        List<ConfigIdGen> idGenList = jdbcTemplate.query("select * from c_id_gen", new Object[0], new int[0], new BeanPropertyRowMapper<>(ConfigIdGen.class));
        idGenList.forEach(c->{
            IDGenerator idGenerator = new IDDoubleBufferGenerator( () -> idGenSegmentService.getIdSegment(c.getBizType()));
            idGeneratorMap.putIfAbsent(c.getBizType(), idGenerator);
        });
    }

    @Override
    public Long getId(String businessType) {
        IDGenerator idGenerator = idGeneratorMap.get(businessType);
        Objects.requireNonNull(idGenerator, "IdGenerator is null");
        return (Long) idGenerator.next();
    }
}
