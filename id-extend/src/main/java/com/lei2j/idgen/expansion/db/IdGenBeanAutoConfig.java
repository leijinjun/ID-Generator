package com.lei2j.idgen.expansion.db;

import com.lei2j.idgen.expansion.db.resource.DefaultIdSegmentResource;
import com.lei2j.idgen.expansion.db.resource.IdSegmentResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * IdGen缺省自动配置
 * @author leijinjun
 * @date 2022/11/14
 **/
@Configuration()
public class IdGenBeanAutoConfig {

    @Bean
    @ConditionalOnMissingBean({JdbcTemplate.class})
    public JdbcTemplate jdbcTemplate(@Autowired DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean({IdSegmentResource.class})
    public IdSegmentResource defaultIdSegmentResource(@Autowired JdbcTemplate jdbcTemplate){
        return new DefaultIdSegmentResource(jdbcTemplate);
    }

    @Bean
    @ConditionalOnBean({IdSegmentResource.class})
    @ConditionalOnMissingBean({IdGen.class})
    public IdGen defaultIdGen(@Autowired IdSegmentResource idSegmentResource){
        return new DefaultIdGen(idSegmentResource);
    }
}
