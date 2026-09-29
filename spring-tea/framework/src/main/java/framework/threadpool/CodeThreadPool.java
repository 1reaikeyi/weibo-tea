package framework.threadpool;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 验证码业务线程池配置（JDK17，纯JDK原生、无第三方线程工厂依赖）
 * 用于短信、邮箱验证码发送IO密集任务
 */
@Slf4j
@AutoConfiguration
public class CodeThreadPool {

    /**
     * IO密集：核心线程 CPU*2
     */
    private final int corePoolSize = 1 * 2;
    /**
     * 最大线程数
     */
    private final int maximumPoolSize = corePoolSize * 2;
    /**
     * 空闲线程存活30秒
     */
    private final long keepAliveTime = 30L;
    /**
     * 等待队列容量，防止任务无限堆积OOM
     */
    private final int queueCapacity = 200;

    private ExecutorService captchaThreadPool;


}
