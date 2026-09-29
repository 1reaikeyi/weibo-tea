package framework.aspect;

import common.annotion.Logging;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Info注解切面类 - 处理@Info注解标记的方法日志记录
 */
@Aspect
@Component
public class InfoAspect {
    private static final Logger log = LoggerFactory.getLogger(InfoAspect.class);

    @Around("@annotation(common.annotion.Logging)")
    public Object interceptServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取注解信息和目标方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 目标方法
        Method targetMethod = signature.getMethod();
        // 获取自定义注解
        Logging annotation = targetMethod.getAnnotation(Logging.class);
        // 注解的描述属性
        String methodDesc = annotation.desc();
        // 目标类名（比如com.rent.service.RentService）
        String className = joinPoint.getTarget().getClass().getName();
        // 目标方法名（比如queryRentInfo）
        String methodName = targetMethod.getName();

        Object result = null;
        try {
            // 3. 执行目标方法（核心业务逻辑）
            result = joinPoint.proceed();
            log.info("=>class执行类: {}, 执行方法: {}, 方法备注: {}", className, methodName, methodDesc);
            log.info(" Rerurn: {}", result);
        } catch (Exception e) {
            // 5. 方法执行异常：打印异常信息
            log.info("=>class执行类: {}, 执行方法: {}, 方法备注: {}", className, methodName, methodDesc);
            log.error("存在异常信息:{}", e.getMessage());
            throw e;
        }
        return result;
    }
}