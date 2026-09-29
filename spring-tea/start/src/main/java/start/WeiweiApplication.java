package start;

import com.branch.properties.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 启动类 - Spring Boot应用入口
 */
@SpringBootApplication(scanBasePackages = {"start","framework","com.branch"})
@Slf4j
@MapperScan("com.branch.mapper")
@EnableConfigurationProperties(JwtProperties.class)
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WeiweiApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeiweiApplication.class, args);
        log.info("---匹配成功");
    }
}