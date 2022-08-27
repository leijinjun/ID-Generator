### ID-Generator
唯一ID生成器，支持数据库方式生成ID、雪花算法。
### 介绍
ID-Core包为id生成算法实现。
**IdGenerator**接口，定义如何获取全局唯一id。
该包核心Id生成器接口，定义如何获取全局唯一id。目前已实现**SegmentIdBufferGenerator**、
**SegmentIdDoubleBufferGenerator**、**SnowFlakeGenerator**。
1. SegmentIdBufferGenerator和SegmentIdDoubleBufferGenerator需要依赖外部资源。
