package aop;


import cn.hutool.core.date.SystemClock;
import cn.hutool.json.JSONUtil;
import constant.Constant;
import context.ContextHolder;
import entity.CommonLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Pointcut("execution(public * async.*(..))")
    public void myPointCut() {
    }

    @Before("myPointCut()")
    public void before(JoinPoint joinPoint) {
        try {
            Object[] arguments = joinPoint.getArgs();
            if (arguments == null) {
                return;
            }
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            ContextHolder.setLog(Constant.API, method.getName());
            ContextHolder.setLog(Constant.TIMESTAMP, String.valueOf(SystemClock.now()));
        } catch (Exception e) {
            log.warn("log request parameters failed", e);
        }
    }

    @AfterReturning(value = "myPointCut()", returning = "result")
    public void saveLog(JoinPoint joinPoint, Object result) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();
        //从切面织入点处通过反射机制获取织入点处的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        //获取切入点所在的方法
        Method method = signature.getMethod();

        //获取请求的类名
        String className = joinPoint.getTarget().getClass().getName();
        //获取请求的方法名
        String methodName = method.getName();
        CommonLog.Builder builder = new CommonLog.Builder();
        long startTime = NumberUtils.toLong(ContextHolder.getLog(Constant.TIMESTAMP));
        CommonLog commonLog = builder
                .api(methodName)
                .service(StringUtils.EMPTY)
                .method(StringUtils.EMPTY)
                .interval(SystemClock.now() - startTime)
                .request(JSONUtil.toJsonStr(joinPoint.getArgs()))
                .response(JSONUtil.toJsonStr(result))
                .traceLogId(ContextHolder.getLog(Constant.TRACE_LOG_ID))
                .timestamp(ContextHolder.getLog(Constant.TIMESTAMP)).build();
        ContextHolder.remove();
        log.info(JSONUtil.toJsonStr(commonLog));
    }
}
