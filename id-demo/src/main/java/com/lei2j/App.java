package com.lei2j;

import com.lei2j.core.IdGenerator;
import com.lei2j.core.snowflake.SnowFlakeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * @author lei2j
 */
@RestController
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    final IdGenerator snowFlakeGenerator = new SnowFlakeGenerator();

    @RequestMapping("/snowflake")
    public Object snowflake() {
        return snowFlakeGenerator.next();
    }
}
