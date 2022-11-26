package com.lei2j.idgen.expansion.db.resource;

import com.lei2j.idgen.expansion.db.ConfigIdGenPO;
import com.lei2j.idgen.core.segment.BizType;
import com.lei2j.idgen.core.segment.ID;
import com.lei2j.idgen.core.segment.IDSegment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 默认服务，
 * 基于Mysql的悲观锁ID资源段实现
 * @author leijinjun
 * @date 2021/10/7
 **/
public class DefaultIdSegmentResource implements IdSegmentResource {

    private final JdbcTemplate jdbcTemplate;

    public DefaultIdSegmentResource(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ID getIdSegment(BizType bizType) {
        final Stream<ConfigIdGenPO> stream = jdbcTemplate.queryForStream("select id,cur_id,step,`version` from c_id_gen where " +
                "biz_type = ? for update", new BeanPropertyRowMapper<>(ConfigIdGenPO.class), bizType.getBusinessType());
        final ConfigIdGenPO configIdGenPo = Objects.requireNonNull(stream.findFirst().orElse(null),
                "bizType:"+bizType+" not found");
        Long curId = configIdGenPo.getCurId();
        Integer step = configIdGenPo.getStep();
        long maxId = curId + step;
        int update = jdbcTemplate.update("update c_id_gen set cur_id = ? , update_at = ? , version = version+1 where id = ? and version = ?",
                new Object[]{maxId, LocalDateTime.now(), configIdGenPo.getId(), configIdGenPo.getVersion()},
                new int[]{Types.BIGINT, Types.TIMESTAMP, Types.BIGINT, Types.BIGINT});
        return new IDSegment(curId, maxId, bizType);
    }

}
