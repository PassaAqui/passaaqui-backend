package com.passaaqui.backend.infra.logging;

import com.passaaqui.backend.infra.exception.ConflictException;
import com.passaaqui.backend.infra.exception.ForbiddenException;
import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.passaaqui.backend..modules..controller..*.*(..))"
            + " || execution(* com.passaaqui.backend..modules..service..*.*(..))")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.info("=> {} argumentos={}", methodName, Arrays.toString(args));

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("<= {} retorno={} tempo={}ms", methodName, result, elapsed);
            return result;
        } catch (Throwable t) {
            long elapsed = System.currentTimeMillis() - start;
            if (isClientError(t)) {
                log.warn("<= {} erro={} tempo={}ms", methodName, t.getMessage(), elapsed);
            } else {
                log.error("<= {} erro={} tempo={}ms", methodName, t.getMessage(), elapsed, t);
            }
            throw t;
        }
    }

    private boolean isClientError(Throwable t) {
        return t instanceof ResourceNotFoundException
                || t instanceof InvalidRequestException
                || t instanceof ConflictException
                || t instanceof ForbiddenException
                || t instanceof MethodArgumentNotValidException
                || t instanceof MethodArgumentTypeMismatchException;
    }
}
