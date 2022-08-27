package com.lei2j.idgen.database.resource;

import com.lei2j.core.idgen.IDSegment;
import com.lei2j.core.idgen.ID;
import com.lei2j.idgen.database.ConfigIdGenPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 基于Mysql的悲观锁ID资源段实现
 * @author leijinjun
 * @date 2021/10/7
 **/
@Service
public class DefaultIdSegmentResource {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Exception.class)
    public ID getIdSegment(String bizType) {
        final Stream<ConfigIdGenPO> stream = jdbcTemplate.queryForStream("select id,cur_id,step,`version` from c_id_gen where " +
                "biz_type = ? for update", new BeanPropertyRowMapper<>(ConfigIdGenPO.class), bizType);
        final ConfigIdGenPO configIdGenPo = Objects.requireNonNull(stream.findFirst().orElse(null), "configIdGenPO is null");
        Long curId = configIdGenPo.getCurId();
        Integer step = configIdGenPo.getStep();
        long maxId = curId + step;
        int update = jdbcTemplate.update("update c_id_gen set cur_id = ? , update_at = ? , version = version+1 where id = ? and version = ?",
                new Object[]{maxId, LocalDateTime.now(), configIdGenPo.getId(), configIdGenPo.getVersion()},
                new int[]{Types.BIGINT, Types.TIMESTAMP, Types.BIGINT, Types.BIGINT});
        return new IDSegment(curId, maxId);
    }

}