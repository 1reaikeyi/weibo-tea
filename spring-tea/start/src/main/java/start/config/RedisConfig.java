package start.config;

import io.lettuce.core.ReadFrom;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    // Redis 节点密码（1 主 + 2 从共用同一个密码）
    @Value("${spring.data.redis.password}")
    private String password;

    // 哨兵监控的 master 名，需与 redis-sentinel.conf 中 "sentinel monitor <name> ..." 保持一致
    @Value("${spring.data.redis.sentinel.master}")
    private String masterName;

    // 3 个哨兵地址，逗号分隔，如 192.168.80.128:26379,192.168.80.129:26379,192.168.80.130:26379
    @Value("${spring.data.redis.sentinel.nodes}")
    private String sentinelNodes;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 使用哨兵模式：客户端通过哨兵自动发现当前 master，并在主节点宕机时自动切换到新主
        var sentinelServerConfig = config.useSentinelServers()
                .setMasterName(masterName)                  // 指定哨兵监控的 master 名
                .setPassword(password)                      // Redis 主/从节点的访问密码
                .setSentinelPassword(password)              // 哨兵自身密码（本项目所有节点密码相同，直接复用）
                .setDatabase(0)                             // 使用的 db（哨兵模式支持 0~15，默认 0）
                .setReadMode(ReadMode.MASTER_SLAVE);        // 读模式：主从都可读，充分利用 2 个从节点

        // 逐个添加 3 个哨兵节点（Spring 配置里是逗号分隔的字符串，需拆分并补 redis:// 前缀）
        for (String node : sentinelNodes.split(",")) {
            String trimmed = node.trim();
            if (!trimmed.isEmpty()) {
                sentinelServerConfig.addSentinelAddress("redis://" + trimmed);
            }
        }

        return Redisson.create(config);
    }

    @Bean
    public LettuceClientConfigurationBuilderCustomizer clientConfigurationBuilderCustomizer() {
        // ReadFrom.REPLICA_PREFERRED: 优先读从节点，从节点不可用时再回退到主节点
        // 注意：此配置仅对 Spring Data Redis(Lettuce / RedisTemplate) 生效，与上面的 Redisson 相互独立
        return builder -> builder.readFrom(ReadFrom.REPLICA_PREFERRED);
    }
}
