<div align="center">
  <h1>Weibo-tea微博-奶茶</h1>
    <h5>Weibo-tea：C2C经营模式，多个商家，多个买家。奶茶团购，由管理员，用户，商家三方组成。微博奶茶，由Spring Boot + Vue 3的前后端分离设计，使用redis中间件+nginx分布式系统。<h5>
    <h1>配置要求</h1>
    <img src="https://img.shields.io/badge/Java-17+ -6DB33F?style=flat-square&logo=java&logoColor=white" alt="Java" />
    <img src="https://img.shields.io/badge/Spring%20Boot-3.+ -6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/MySQL-8.0+ -6DB33F?style=flat-square&logo=mysql&logoColor=white" alt="mysql" />
    <img src="https://img.shields.io/badge/Redis-7.0+ -6DB33F?style=flat-square&logo=redis&logoColor=white" alt="redis" />
    <img src="https://img.shields.io/badge/Spring%20AI-1.1.+ -6DB33F?style=flat-square&logo=spring&logoColor=white" alt="spring ai" />
    <img src="https://img.shields.io/badge/Vue-Node.js20.+ -6DB33F?style=flat-square&logo=vuedotjs&logoColor=white" alt="vue" />
</div>


## 项目结构

weibo-tea/说明/wiki.md

## 接口文档



# 前端功能

## 管理端

## 用户端

------

# 后端说明

### 组件：Redis分布式ID生成器（RedisID）

```java
@Component
public class RedisID {
    // 基准时间：2020-01-01 00:00:00 UTC
    private final static long BEGIN_TIME = 1577836800L;
    // 32位序号最大值
    private static final long MAX_SEQ = 0xFFFFFFFFL;

    public long createId(String prefix) {
        long nowSeconds = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond();
        long timestamp = nowSeconds - BEGIN_TIME;
        
        String date = ZonedDateTime.now(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        String key = "icr:" + prefix + ":" + date;
        long count = stringRedisTemplate.opsForValue().increment(key);
        
        return timestamp << 32 | count;
    }
}
```

Q：为什么不用UUID？

> A：UUID是随机字符串，无序，作为数据库主键会导致索引分裂，影响性能。而且UUID太长（36位），存储和传输成本高。

Q：为什么不用数据库自增ID？

> A：数据库自增ID在分布式环境下需要额外处理（比如分库分表），而且生成ID需要访问数据库，性能不如Redis。

Q：ID结构为什么是 1位符号位+时间戳(31位) + 序号(32位)？

> A：0作为符号位，正数自增，31位时间戳可以表示约68年（2^31秒 ≈ 68年），从2020年开始够用。32位序号可以表示约42亿，足够单日并发使用

---



## 一、用户管理模块

### **策略流程图**

```java
用户注册 → UserController/register() → 加密密码 → MySQL保存用户 → 返回注册成功
用户登录 → UserController/login() → 校验用户名密码 → 生成JWT Token → Redis存储Token → 返回Token
请求拦截 → 直接拦截脚本等操作LoginInterceptor/对于活跃用户刷新ReLoginInterceptor → 校验Token → 滑动过期刷新 → 放行请求
```

### 问题修复阶段

Q：为什么jwt要用redis存储？

> | 维度         | 服务端 Session                | Redis                   |
> | ------------ | ----------------------------- | ----------------------- |
> | 部署架构     | 单体友好，集群麻烦            | 天生适配分布式、微服务  |
> | 存储位置     | 应用服务器内存                | 独立中间件 Redis        |
> | 客户端适配   | 依赖 Cookie，APP / 小程序难用 | Header 传输，全终端兼容 |
> | 服务重启影响 | 全部用户掉线                  | 不受影响                |
> | 强制下线     | 实现复杂                      | 直接删除 key，简单      |
> | 横向扩容     | 差                            | 优秀                    |
> | 跨域场景     | Cookie 跨域限制多             | 无 Cookie 限制          |

Q：账户的安全性，为啥放弃传统MD5加密?

>  1**面对AI与GPU海量算力，MD5算力防御几乎失效** 单张高端显卡每秒可完成上千亿次MD5哈希运算。攻击者借助AI生成智能字典、搭配GPU集群并行枚举，即便加盐，依然可以高速批量尝试口令。加盐只能抵御彩虹表，**无法降低单次哈希的运算速度**。 而BCrypt提供可调节的工作因子（Cost），人为拉长单次哈希耗时，大幅抬升攻击者算力成本。正常用户登录感知不到几十毫秒延迟，但会让暴力破解效率下降数十万倍。 2. **盐值管理存在工程风险** MD5+外置盐需要开发者手动实现盐生成、持久化、加密拼接逻辑，极易出现盐重复、盐长度不足等漏洞；BCrypt自动为每个用户生成独立随机盐，盐直接内嵌在密文字符串中，不需要额外设计数据库盐字段，由SpringSecurity原生封装，规避人为编码失误。

Q:Token过期时间固定，用户活跃时Token也会过期

> 实现滑动过期策略，在ReLoginInterceptor中每次请求时刷新Redis中Token的有效期

```java
// ReLoginInterceptor.java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String token = request.getHeader("Authorization");
    Map<String, Object> claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
    String currentId = claims.get(JwtConstant.ID).toString();
    Long id = Long.parseLong(currentId);
    
    // 验证Token是否与Redis中存储的一致
    String standard_token = stringRedisTemplate.opsForValue().get("bigevent:" + id);
    if (!standard_token.equals(token)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
    
    // 刷新Token有效期（滑动过期策略）
    stringRedisTemplate.expire("bigevent:" + id, jwtProperties.getTtlMillis(), TimeUnit.SECONDS);
    
    // 将用户信息存入ThreadLocalContextHolder
    ThreadLocalContextHolder.set(claims);
    return true;
}
```

---

## 二、文章管理模块

### **策略流程图**

![缓存](说明/原型功能/缓存.png)

```java
查询文章 → ArticleController → ArticleServiceImpl → Redis查询缓存
    ├─ 缓存存在且未过期 → 直接返回缓存数据
    ├─ 缓存存在但已过期 → RedisLock分布式锁 → 查询数据库 → 更新缓存 → 返回新数据
    └─ 缓存不存在 → 查询数据库 → 设置逻辑过期缓存 → 返回数据
更新文章 → ArticleController → ArticleServiceImpl → 更新MySQL → 删除Redis缓存
```

### 问题修复阶段

Q：为什么用逻辑过期而不是物理过期？

> A：物理过期的话，缓存过期瞬间会有大量请求穿透到数据库（缓存击穿）。逻辑过期是缓存永不过期，但在数据中记录过期时间，过期后通过分布式锁让一个线程去更新缓存，其他线程返回旧数据，这样不会导致数据库压力骤增。

Q：为什么不直接用@Cacheable注解？

> A：@Cacheable是Spring提供的声明式缓存，虽然方便但不够灵活。比如需要自定义缓存策略、分布式锁控制、逻辑过期等场景，手动控制Redis操作更合适。

Q: 缓存击穿问题 - 热点文章缓存过期瞬间，大量请求同时穿透到数据库

> 使用分布式锁（RedisLock）+ 逻辑过期策略

---

## 三、分类管理模块

### **策略流程图**

```java
查询分类 → CategoryController → CategoryServiceImpl → Redis查询缓存
    ├─ 缓存存在且未过期 → 直接返回缓存数据
    ├─ 缓存存在但已过期 → RedisLock分布式锁 → 查询数据库 → 更新缓存 → 返回新数据
    └─ 缓存不存在 → 查询数据库 → 设置逻辑过期缓存 → 返回数据
更新分类 → CategoryController → CategoryServiceImpl → 更新MySQL → 删除Redis缓存
```

### 问题修复阶段

Q：为什么复用文章模块的缓存策略？

> A：分类数据和文章数据的缓存需求相似——都是读多写少、需要防止缓存击穿。复用相同的逻辑过期+分布式锁策略可以减少代码重复，提高可维护性。

Q：分类和文章的缓存策略有什么差异？

> A：分类数据量更小（通常几十到几百个），缓存命中率更高，可以设置更长的逻辑过期时间。而文章数据量大，需要更频繁地更新缓存。

Q: 分类修改后，文章页面显示的分类名称没有更新

> 在分类更新/删除时主动删除缓存，确保下次查询时从数据库获取最新数据

```java
@Override
public Boolean updateCache(Category category) {
    String key = KEYS + category.getId();
    boolean result = super.updateById(category);
    stringRedisTemplate.delete(key);  // 删除缓存
    return result;
}
```

---

## 四、探店博文模块

### **策略流程图**

```java
点赞请求 → BlogController/likes() → 更新MySQL点赞数 → Redis ZSet记录点赞用户（score=timestamp）→ 返回结果
取消点赞 → BlogController/likes() → 更新MySQL点赞数 → Redis ZSet移除点赞用户 → 返回结果
查询点赞状态 → Redis ZSet/ZScore判断用户是否存在
查询热门点赞 → Redis ZSet/ZRange获取Top N用户ID → 查询用户信息 → 返回结果
```

### 修复阶段

Q：为什么点赞用Redis的ZSet而不是普通Set？

> A：ZSet可以存储分数（timestamp），这样可以按点赞时间排序，方便获取热门点赞用户。同时ZSet的score操作是原子的，不会出现并发问题。

Q：为什么点赞数同时存Redis和MySQL？

> A：Redis用于实时查询和计数，MySQL用于持久化存储。点赞操作先更新MySQL再更新Redis，保证数据最终一致性。

Q:点赞操作在高并发下可能出现计数不准确

> MySQL使用原子操作 `setSql("liked= liked + 1")`，Redis使用ZSet的原子add/remove操作，保证计数一致性。

---

## 五、评论与回复模块

### **策略流程图**

```java
发表评论 → BlogCommentsController/save() → 设置parent_id=0 → MySQL保存 → 返回结果
回复评论 → BlogCommentsController/save() → 设置parent_id=1，answer_id=目标评论ID → MySQL保存 → 返回结果
查询评论列表 → BlogCommentsController/list() → MySQL按blog_id分页查询 → 返回评论列表（含回复）
点赞评论 → BlogCommentsController/likes() → MySQL更新点赞数 → 返回结果
```

### 修复阶段

Q：为什么用parent_id区分评论和回复？

> A：`parent_id = 0` 表示直接评论博文，`parent_id = 1` 表示回复其他评论。这种设计可以支持无限层级的回复，同时查询时可以通过parent_id区分评论和回复。

Q：评论表为什么需要answer_id字段？

> A：`answer_id` 记录回复目标评论的ID，用于构建评论的回复链，方便前端展示回复关系。

Q：评论状态管理（正常、被举报、禁止查看）

> 修复方案：在blog_comments表中设置status字段，0表示正常，1表示被举报，2表示禁止查看。查询时过滤掉status=2的评论。

---

## 六、文件管理模块

### **策略流程图**：

```java
文件上传（本地）→ FileController/upload() → UUID生成文件名 → 保存到本地目录 → 返回本地访问URL
文件上传（阿里云OSS）→ FileOssController/upload() → UUID生成文件名 → AliOssUtil上传 → 返回CDN访问URL
文件下载（本地）→ FileController/download() → 读取本地文件 → 设置Content-Disposition → 返回文件流
文件下载（阿里云OSS）→ FileOssController/download() → AliOssUtil下载 → 返回文件流
```

### 修复阶段

Q：为什么提供两种文件存储方式？

> A：本地存储用于开发测试环境，简单快捷；阿里云OSS用于生产环境，支持高可用和CDN加速。

Q：文件命名为什么用UUID？

> A：UUID全局唯一，避免文件名冲突，同时增加安全性（防止文件遍历攻击）。

Q：文件下载中文文件名乱码

修复方案：使用URLEncoder编码文件名，同时设置Content-Disposition响应头

```java
response.setHeader("Content-Disposition", "attachment;filename=" + 
    URLEncoder.encode(fileName, StandardCharsets.UTF_8));
```

---

## 七、优惠券使用的并发模块

### 秒杀策略流程图

#### 同步版本流程

```
用户请求 → 校验秒杀活动 → 生成订单ID → 直接调用secondKill() → 扣库存+保存订单 → 返回结果
```

#### 异步单机流程

```
用户请求 → 校验秒杀活动 → Lua脚本校验 → 放入ArrayBlockingQueue → 返回订单ID
                                                              ↓
                                        后台线程 take() → RedisLock → paySuccess() → 扣库存+保存订单
```

#### 异步分布式流程

```
用户请求 → 校验秒杀活动 → Lua脚本校验（自动XADD到Stream）→ 返回订单ID
                                                         ↓
                                XREADGROUP读取 → 解析订单 → secondKill() → 扣库存+保存订单 → ACK确认
```

#### 数据库操作阶段

无论是同步还是异步，最终都需要执行以下数据库操作：

1. **一人一单校验**：在事务中基于用户ID和优惠券ID查询已存在订单
2. **原子库存扣减**：使用 MyBatis Plus 的乐观锁 CAS 操作保证库存扣减的原子性
3. **订单创建**：保存订单记录

> **注意**：同步版本直接在请求线程中执行数据库操作，而异步版本在后台线程池中执行。

```
用户请求 → Lua脚本校验（库存+重复下单）→ 成功
								  ↓ →放入异步队列 → 后台线程处理（扣库存+保存订单）
                                  ↓ →失败→直接返回
```

**Lua脚本（redis-seckill.lua）**：

```lua
-- 参数：优惠券ID、用户ID
local voucherId = ARGV[1]
local userId = ARGV[2]

-- Redis Key定义
local stockKey = "voucherSeckill:stock:" .. voucherId
local orderKey = "voucherSeckill:order:" .. voucherId

-- 1. 判断库存
if tonumber(redis.call('get', stockKey)) <= 0 then
    return 1  -- 库存不足
end

-- 2. 判断是否已下单
if redis.call('sismember', orderKey, userId) > 0 then
    return 2  -- 重复下单
end

-- 3. 扣减库存
redis.call('incrby', stockKey, -1)

-- 4. 记录下单用户
redis.call('sadd', orderKey, userId)

-- 5. 成功
return 0
```

**适用场景**：中等并发场景（单机几千QPS），内存队列速度快，但重启后队列数据会丢失。

###  问题修复阶段

Q：库存超卖

修复方案：Redis中用Lua脚本原子扣减,MySQL中用乐观锁 `gt(stock, 0).setSql("stock = stock - 1")`

Q：重复下单

修复方案：Redis中用Set存储已下单用户ID（sismember判断）,MySQL中查询已有订单记录

Q：分布式锁误删

修复方案：使用Lua脚本释放锁，只有锁的持有者才能释放

Q：为什么用Lua脚本？

> A：Lua脚本可以保证多个Redis命令的原子性执行，避免竞态条件。比如扣库存和判断一人一单必须同时成功或同时失败。

Q：为什么要异步处理订单？

> A：如果同步处理，用户下单请求需要等待数据库操作完成，响应时间长。异步处理可以先返回订单ID，后台线程慢慢处理数据库写入，提升用户体验。

### 三种架构对比

| 维度 | VoucherSeckillController | VoucherController | VoucherOrderController |
| :--- | :--- | :--- | :--- |
| **处理方式** | 同步 | 异步 | 异步（Redis Stream） |
| **队列类型** | 无 | ArrayBlockingQueue | Redis Stream |
| **分布式锁** | Redisson | 自定义RedisLock | Redisson |
| **消息持久化** | 无 | 无 | 有 |
| **多实例支持** | 支持（锁保证） | 不支持（内存队列） | 支持（消费组） |
| **吞吐量** | 低（几百QPS） | 中（几千QPS） | 高（几万QPS） |
| **故障恢复** | 无状态 | 队列数据丢失 | 消息可恢复 |
| **适用场景** | 测试/低并发 | 中等并发 | 生产高并发 |

---

## 八、邮箱登录与验证码模块

![验证码](说明/原型功能/验证码.png)

### **策略流程图**：

```java
发送验证码 → LoginController/sendCode() → 生成6位验证码 → Redis存储（10分钟过期）→ XADD到Redis Stream → 返回结果
                                               ↓
                                       后台线程 XREADGROUP读取 → JavaMailSender发送邮件 → ACK确认
邮箱登录 → LoginController/loginByEmail() → Redis校验验证码 → 查询用户 → 生成JWT Token → 返回Token
```

### 修复阶段

Q：为什么用Redis Stream异步发送邮件？

> A：邮件发送是IO密集型操作，直接在请求线程中发送会导致响应时间过长。使用Redis Stream作为消息队列，可以实现异步解耦，请求线程只负责生成验证码并存入队列，后台线程负责实际发送邮件。

Q：为什么验证码存在Redis而不是数据库？

> A：验证码是短期临时数据（10分钟过期），存入Redis可以利用其过期自动清理的特性，无需额外维护清理任务，且读写性能更高。

Q：Stream消费组重复创建异常

> 修复方案：在 `@PostConstruct` 初始化方法中捕获异常，若消费组已存在则忽略错误

```java
@PostConstruct
public void init() {
    try {
        stringRedisTemplate.opsForStream().createGroup(STREAM_KEY, "g1");
        log.info("Redis Stream消费组创建成功");
    } catch (Exception e) {
         //重复的测试group会重复创建，有异常
        log.info("消费组已存在，跳过创建");
    }
    CODE_EXECUTOR.submit(new HandleCodeTask());
}
```

Q：应用关闭时线程池未正确关闭

> 修复方案：在 `@PreDestroy` 方法中优雅关闭线程池

```java
@PreDestroy
public void destroy() {
    CODE_EXECUTOR.shutdown();
    try {
        if (!CODE_EXECUTOR.awaitTermination(10, TimeUnit.SECONDS)) {
            CODE_EXECUTOR.shutdownNow();
        }
    } catch (InterruptedException e) {
        CODE_EXECUTOR.shutdownNow();
        Thread.currentThread().interrupt();
    }
}
```

---

## 九、关注管理模块

### **策略流程图**

```java
关注请求 → UseFollowController/useFollow() → MySQL保存关注关系 → Redis Set添加（follow:{userId}）→ 返回结果
取关请求 → UseFollowController/useFollow() → MySQL删除关注关系 → Redis Set移除（follow:{userId}）→ 返回结果
查询关注状态 → UseFollowController/getUserFollow() → Redis Set/IsMember判断 → 返回结果
查询共同关注 → UseFollowController/getUserFollowCommon() → Redis Set/ZIntersect → 查询用户信息 → 返回结果
```

### 修复阶段

Q：为什么用Redis Set存储关注关系？

> A：Set支持高效的集合操作（add、remove、contains、intersect），非常适合实现关注关系的管理。共同关注功能可以通过Set的intersect操作快速获取两个用户关注集合的交集。

Q：为什么同时更新数据库和Redis？

> A：数据库用于持久化存储，保证数据不丢失；Redis用于高性能查询，提高关注状态查询和共同关注计算的响应速度。采用双写策略，先写数据库再写Redis。

Q：关注状态返回不够直观 ✅ 已修复

> 修复方案：返回更明确的状态描述（"已关注"/"未关注"），已在 `UseFollowController.java` 中应用

```java
// 将用户ID转换为用户信息列表
 List<Long> ids = commonSet.stream().map(s -> Long.parseLong(s)).toList();
//降低负载，ids = 一次
 List<User> userList = userService.listByIds(ids);
```

---

## 十、签到管理模块

**Redis Key设计**：

```
sign:{userId}:{yyyy-MM}  // 用户签到位图Key，例如 sign:1:2024-01
```

### 策略流程图

```java
签到请求 → SignController/createSign() → Redis SetBit设置签到位（offset=日期天数）→ 返回结果
补签请求 → SignController/backSign() → Redis SetBit设置指定日期签到位 → 返回结果
统计请求 → SignController/CountSign() → Redis BitField获取位图 → 统计1的个数 → 返回签到/缺勤数
保存请求（按月保存） → SignController/CountSign() → Redis BitField获取位图 → MySQL保存统计数据
```

### 修复阶段

Q：为什么用Redis BitMap存储签到记录？

> A：BitMap（位图）是一种高效的位存储结构，每个用户每天的签到状态只需要1个位（0或1）。一个月最多31天，只需要31个位（约4字节）就能存储一个用户一个月的签到记录，极大节省存储空间。

Q：为什么用bitField命令统计签到次数？

> A：bitField可以批量获取位图中的位数据，将指定位数的二进制数据转换为十进制数，然后通过统计二进制中1的个数来快速计算签到天数。
>
> ```
> 第一种long signedDays = Long.bitCount(num10);
> 第二种for (int i = 0; i < now.getDayOfMonth(); i++){
>          if ((num10 & 1) == 1){
>              signedDays++;
>          }
>          num10 = num10 >>>1;
>      }
> ```

Q：签到统计时，如果当月没有任何签到记录，result为空导致空指针异常 ✅ 已修复

> 修复方案：使用 `CollectionUtil.isEmpty()` 判断结果是否为空，为空时返回0

```java
if (CollectionUtil.isEmpty(result)) {
    return Result.success(0);
}
```

Q：补签接口未校验日期是否合法（如日期格式错误、日期超出当月范围）

> 修复方案：在补签接口中添加日期格式校验和范围校验，防止非法日期操作

---

## 十一、店铺搜索模块

### **策略流程图**

```java
创建店铺 → ShopController/createShop() → MySQL保存Shop和ShopType → Redis GEO存储位置（shopType:{typeId}）→ 返回结果
查询店铺列表 → ShopController/ofType()
    ├─ 无经纬度 → MySQL游标分页查询（按ID升序，每页5条）→ 返回结果
    └─ 有经纬度 → Redis GEO搜索（5公里范围，按距离排序）→ 获取店铺ID列表 → MySQL查询详情 → 返回结果
```

### 修复阶段

Q：为什么用Redis GEO存储店铺位置？

> A：Redis GEO是专门为地理位置数据设计的数据结构，支持高效的距离计算和范围查询。使用GEO可以快速找到指定坐标附近的店铺，并且按距离排序，这是传统数据库难以实现的。

Q：为什么设计两种查询模式？

> A：当用户未提供位置信息时，使用基于ID的游标分页（滚动分页），简单高效；当用户提供经纬度时，使用Redis GEO按距离排序查询附近店铺，满足LBS（位置服务）需求。

Q：Redis GEO搜索结果为空时可能导致空指针异常

> 修复方案：使用 `CollectionUtil.isEmpty()` 判断结果是否为空，为空时直接返回空列表

```java
if (CollectionUtil.isEmpty(results)){
    return Result.success(null);
}
```

---

