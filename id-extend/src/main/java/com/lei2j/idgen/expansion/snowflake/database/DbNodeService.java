package com.lei2j.idgen.expansion.snowflake.database;

import com.lei2j.idgen.expansion.LocalIpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * @author leijinjun
 * @date 2022/11/18
 **/
public class DbNodeService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Exception.class)
    public long getWorkId(String namespace, long maxWorkId, String locker, Duration duration) {
        String sql = "select max(work_id) from c_namespace_node where namespace=? for update";
        final Long currentWorkId = jdbcTemplate.queryForObject(sql, Long.class, namespace);
        Long workId = null;
        if (currentWorkId == null || currentWorkId < maxWorkId) {
            workId = currentWorkId == null ? 1 : currentWorkId + 1;
            String insertSql = "insert into c_namespace_node(id,namespace,host_name,host,expired,work_id,locker,create_at,update_at) " +
                    "values (?,?,?,?,?,?,?,?,?)";
            jdbcTemplate.update(insertSql, null, namespace, getHostName(), LocalIpUtils.getLocalIp(""),
                    LocalDateTime.now().plusSeconds(duration.getSeconds()), workId, locker, LocalDateTime.now(), LocalDateTime.now());
            return workId;
        } else {
            //查找过期节点
            String selectSql = "select * from c_namespace_node where namespace=? and work_id<? and expired < now() " +
                    "limit 1 for update";
            final List<NamespaceNodePO> nodeList = jdbcTemplate.query(selectSql,
                    new BeanPropertyRowMapper<>(NamespaceNodePO.class), namespace, maxWorkId);
            if (nodeList.isEmpty()) {
                throw new RuntimeException("没有剩余的节点可分配，请检查！");
            }
            nodeList.sort(Comparator.comparing(NamespaceNodePO::getWorkId));
            NamespaceNodePO namespaceNode = nodeList.get(0);
            nodeList.clear();
            String updateSql = "update c_namespace_node set host_name=?,host=?,expired=?,locker=?,update_at=?,version=version+1 " +
                    " where id=? and version=?";
            final int updateCount = jdbcTemplate.update(updateSql, getHostName(), LocalIpUtils.getLocalIp(""),
                    LocalDateTime.now().plusSeconds(duration.getSeconds()), LocalDateTime.now(),locker,
                    namespaceNode.getId(), namespaceNode.getVersion());
            if (updateCount > 0) {
                workId = namespaceNode.getWorkId();
            }
            if (workId == null) {
                throw new RuntimeException("获取节点失败");
            }
        }
        return workId;
    }

    public void keepLiveNode(String namespace, Long workId, String locker,
                             Duration keepAliveTimeout, Duration addTime) {
        String sql = "select * from c_namespace_node where namespace=? and work_id=? and locker=?";
        final NamespaceNodePO namespaceNode = jdbcTemplate.queryForObject(sql,
                new BeanPropertyRowMapper<>(NamespaceNodePO.class),
                namespace, workId, locker);
        if (namespaceNode != null) {
            final LocalDateTime expired =
                    Optional.ofNullable(namespaceNode.getExpired()).orElse(LocalDateTime.now().plusSeconds(keepAliveTimeout.getSeconds()));
            if (expired.compareTo(LocalDateTime.now()) < 0) {
                throw new RuntimeException("workId is expired");
            }
            jdbcTemplate.update("update c_namespace_node set version=version+1,expired=?,update_at=? where " +
                            "id=? and version=?", expired.plusSeconds(addTime.getSeconds()), LocalDateTime.now(),
                    namespaceNode.getId(), namespaceNode.getVersion());
        }
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
}
