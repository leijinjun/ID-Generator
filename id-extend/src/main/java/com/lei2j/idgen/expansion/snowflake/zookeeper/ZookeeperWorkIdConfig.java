package com.lei2j.idgen.expansion.snowflake.zookeeper;

import com.lei2j.idgen.expansion.snowflake.WorkIdConfig;
import com.lei2j.idgen.expansion.snowflake.autoconfig.SnowflakeProperties;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 采用zookeeper生成节点id
 * @author leijinjun
 * @date 2022/11/12
 **/
public class ZookeeperWorkIdConfig implements WorkIdConfig, ApplicationContextAware, DisposableBean {

    private static final String ROOT_PATH = "/snowflake";

    /**
     * 心跳间隔
     */
    private final Duration heartbeatTime = Duration.ofSeconds(60);

    private Long workId;

    /**
     * 节点自身
     */
    private String workNode;

    private ApplicationContext applicationContext;

    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private CuratorFramework curatorFramework;

    public ZookeeperWorkIdConfig() {
    }

    @Override
    public void destroy() throws Exception {
        if (curatorFramework != null) {
            curatorFramework.close();
            scheduledExecutorService.shutdownNow();
        }
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public synchronized long initWorkId() throws Exception {
        if (workId == null) {
            curatorFramework.start();
            final SnowflakeProperties snowflakeProperties = applicationContext.getBean(SnowflakeProperties.class);
            String selfHost = snowflakeProperties.getSelfHost();
            if (selfHost == null || selfHost.isEmpty()) {
                throw new Exception("Property selfHost is empty");
            }
            if (selfHost.contains("localhost") || selfHost.contains("127.0.0.1")) {
                throw new Exception("Property selfHost cannot be a local loopback address");
            }
            String nodePrefix = "workId" + MessageFormat.format("[{0}]", selfHost);
            final Stat path = curatorFramework.checkExists().forPath(getPath(ROOT_PATH));
            String nodePath;
            if (path == null) {
                //创建节点
                nodePath = curatorFramework.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                        .forPath(getPath(ROOT_PATH, nodePrefix + "-"),
                                String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
            } else {
                final List<String> childList = curatorFramework.getChildren().forPath(ROOT_PATH);
                final Optional<String> optional = childList.stream().filter(p -> {
                    final String[] split = p.split("-");
                    return Objects.equals(split[0], nodePrefix);
                }).findFirst();
                //该节点已经注册过
                if (optional.isPresent()) {
                    nodePath = getPath(ROOT_PATH, optional.get());
                    final byte[] data = curatorFramework.getData().forPath(nodePath);
                    final long lastTimeStamp = Long.parseLong(new String(data, StandardCharsets.UTF_8));
                    if (System.currentTimeMillis() < lastTimeStamp) {
                        throw new Exception("System time regress");
                    }
                } else {
                    nodePath = curatorFramework.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                            .forPath(getPath(ROOT_PATH, nodePrefix + "-"),
                            String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
                }
            }
            this.workNode = nodePath;
            workId = Long.parseLong(nodePath.split("-")[1]);
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(this::keepLive, 60, heartbeatTime.getSeconds(), TimeUnit.SECONDS);
        }
        return this.workId;
    }

    private String getPath(String... paths) {
        if (paths == null) {
            return null;
        }
        final StringBuilder builder = new StringBuilder();
        for (String path : paths) {
            if (path.startsWith("/")) {
                builder.append(path);
            } else {
                builder.append("/").append(path);
            }
        }
        return builder.toString();
    }

    @Override
    public void keepLive() {
        try {
            curatorFramework.setData().forPath(getPath(workNode),
                    String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
