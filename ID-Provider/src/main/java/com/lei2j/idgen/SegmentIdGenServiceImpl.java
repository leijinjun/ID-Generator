package com.lei2j.idgen;

import com.lei2j.IdGenService;
import com.lei2j.core.IdGenerator;
import com.lei2j.core.idgen.SegmentIdDoubleBufferGenerator;
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
public class SegmentIdGenServiceImpl implements IdGenService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Map<String,IdGenerator> idGeneratorMap = new HashMap<>();

    @Autowired
    IdGenSegmentServiceImpl idGenSegmentService;

    @PostConstruct
    private void setUp() {
        List<ConfigIdGen> idGenList = jdbcTemplate.query("select * from c_id_gen", new Object[0], new int[0], new BeanPropertyRowMapper<>(ConfigIdGen.class));
        idGenList.forEach(c->{
            IdGenerator idGenerator = new SegmentIdDoubleBufferGenerator( () -> idGenSegmentService.getIdSegment(c.getBizType()));
            idGeneratorMap.putIfAbsent(c.getBizType(), idGenerator);
        });
    }

    @Override
    public Long getId(String businessType) {
        IdGenerator idGenerator = idGeneratorMap.get(businessType);
        Objects.requireNonNull(idGenerator, "IdGenerator is null");
        return (Long) idGenerator.next();
    }
}
