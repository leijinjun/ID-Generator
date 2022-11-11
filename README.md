### ID-Generator
唯一ID生成器，支持数据库方式生成ID、雪花算法。
### 介绍
id-core包为id生成算法实现。
ID资源：指id源。如数据库、Redis、应用程序等可以产生ID的源。ID源需保证生产的ID在所需要的应用程序内唯一。
1. **IdGenerator**接口。定义获取全局唯一id。
**SegmentIdBufferGenerator**、**SegmentIdDoubleBufferGenerator**、**SnowFlakeGenerator**为其几种实现。SegmentIdBufferGenerator和SegmentIdDoubleBufferGenerator需要依赖外部ID资源。
2. **IDResource**接口。定义Id资源器。
3. **SnowFlakeGenerator**雪花ID生成器。
### 快速开始
id-extension模块
1. 生成雪花ID
```java
final IdGenerator snowFlakeGenerator = new SnowFlakeGenerator();
final Long next = (Long) snowFlakeGenerator.next();
```
2. 从数据库生成ID
```java
/**
 * 基于Mysql的悲观锁ID资源器实现
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

@Component
public class DefaultIdGen {

    private final DefaultIdSegmentResource resource;

    public DefaultIdGen(DefaultIdSegmentResource resource) {
        this.resource = resource;
    }

    public Object next(String businessType) {
        final IDResource idResource = () -> resource.getIdSegment(businessType);
        final SegmentIdBufferGenerator generator = new SegmentIdBufferGenerator(idResource);
        return generator.next();
    }
}

//id资源器
DefaultIdSegmentResource resource = new DefaultIdSegmentResource();
//将Id资源器与IdGenerator绑定
DefaultIdGen idGen = new DefaultIdGen();
//获取id
Object next = idGen.next('businessType');
```