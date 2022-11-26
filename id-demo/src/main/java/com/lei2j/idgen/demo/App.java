package com.lei2j.idgen.demo;

import com.lei2j.idgen.core.segment.BizType;
import com.lei2j.idgen.core.snowflake.SnowFlakeConfig;
import com.lei2j.idgen.core.snowflake.SnowFlakeGenerator;
import com.lei2j.idgen.expansion.db.IdGen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lei2j
 */
@Configuration
@RestController
@EnableTransactionManagement
@ComponentScan(basePackages = {"com.lei2j.idgen"})
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public SnowFlakeGenerator getSnowFlakeGenerator(SnowFlakeConfig snowFlakeConfig){
        return new SnowFlakeGenerator(snowFlakeConfig);
    }

    @Autowired
    private SnowFlakeGenerator snowFlakeGenerator;

    @RequestMapping("/snowflake")
    public Object snowflake() {
        return snowFlakeGenerator.next();
    }

    @Autowired
    private IdGen idGen;

    @RequestMapping("/idgen")
    public Object idgen(){
        return idGen.next(new BizType("test"));
    }
}
