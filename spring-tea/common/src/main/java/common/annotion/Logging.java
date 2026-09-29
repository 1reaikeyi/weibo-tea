package common.annotion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法日志注解 - 用于标记需要记录日志的方法
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Logging {
    /**
     * 自定义属性：记录
     */
    String desc() default "未描述";
}