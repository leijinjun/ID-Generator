package com.lei2j;

import com.lei2j.core.id.gen.SerialNo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author leijinjun
 * @date 2021/10/7
 **/
@Service
@Transactional
public class IdGenSegmentServiceImpl {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public SerialNo getIdSegment(String bizType) {
        int loop = 0;
        while (loop < 5) {
            ConfigIdGen configIdGen = jdbcTemplate.queryForObject("select id,cur_id,step,`version` from c_id_gen where biz_type = ?",
                    new Object[]{bizType},
                    new int[]{Types.VARCHAR},
                    new BeanPropertyRowMapper<>(ConfigIdGen.class)
            );
            Objects.requireNonNull(configIdGen, "configIdGen is null");
            Long curId = configIdGen.getCurId();
            Integer step = configIdGen.getStep();
            long maxId = curId + step;
            int update = jdbcTemplate.update("update c_id_gen set cur_id = ? , update_at = ? , version = version+1 where id = ? and version = ?",
                    new Object[]{maxId, LocalDateTime.now(), configIdGen.getId(), configIdGen.getVersion()},
                    new int[]{Types.BIGINT, Types.TIMESTAMP, Types.BIGINT, Types.BIGINT});
            if (update == 0) {
                loop++;
            } else {
                return new IDSegment(curId, maxId);
            }
        }
        throw new RuntimeException("don't get Id segment");
    }

}
