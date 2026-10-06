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

import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Set;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    private static final Set<String> SENSITIVE_PARAM_NAMES = Set.of(
            "password", "confirmpassword", "confirm_password", "token", "refreshtoken", "refresh_token", "secret"
    );

    @Around("execution(* com.passaaqui.backend..modules..controller..*.*(..))"
            + " || execution(* com.passaaqui.backend..modules..service..*.*(..))")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = null;

        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            paramNames = methodSignature.getParameterNames();
        }

        Object[] maskedArgs = maskArgs(paramNames, args);

        log.info("=> {} arguments={}", methodName, Arrays.toString(maskedArgs));

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            Object maskedResult = maskResult(result);
            log.info("<= {} return={} elapsed={}ms", methodName, maskedResult, elapsed);
            return result;
        } catch (Throwable t) {
            long elapsed = System.currentTimeMillis() - start;
            if (isClientError(t)) {
                log.warn("<= {} error={} elapsed={}ms", methodName, t.getMessage(), elapsed);
            } else {
                log.error("<= {} error={} elapsed={}ms", methodName, t.getMessage(), elapsed, t);
            }
            throw t;
        }
    }

    private Object[] maskArgs(String[] paramNames, Object[] args) {
        if (args == null) {
            return null;
        }

        Object[] masked = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            String paramName = (paramNames != null && i < paramNames.length) ? paramNames[i] : null;

            if (isSensitiveParam(paramName)) {
                masked[i] = "[PROTECTED]";
            } else if (arg instanceof String str && isJwt(str)) {
                masked[i] = "[PROTECTED]";
            } else {
                masked[i] = maskSensitiveObject(arg);
            }
        }
        return masked;
    }

    private boolean isSensitiveParam(String name) {
        if (name == null) return false;
        String normalized = name.toLowerCase().replaceAll("[^a-z]", "");
        for (String sensitive : SENSITIVE_PARAM_NAMES) {
            if (normalized.contains(sensitive.replaceAll("[^a-z]", ""))) {
                return true;
            }
        }
        return false;
    }

    private boolean isJwt(String str) {
        return str != null && str.length() > 50 && (str.startsWith("eyJ") || str.split("\\.").length == 3);
    }

    private Object maskSensitiveObject(Object obj) {
        if (obj == null) return null;
        if (obj instanceof com.passaaqui.backend.modules.auth.dto.LoginDTO loginDTO) {
            return "LoginDTO[email=" + loginDTO.email() + ", password=[PROTECTED]]";
        }
        if (obj instanceof com.passaaqui.backend.modules.auth.dto.RegisterTouristDTO dto) {
            return "RegisterTouristDTO[email=" + dto.email() + ", name=" + dto.name()
                    + ", password=[PROTECTED], confirm_password=[PROTECTED], documentId=" + dto.documentId() + "]";
        }
        if (obj instanceof com.passaaqui.backend.modules.auth.dto.RegisterAdminDTO dto) {
            return "RegisterAdminDTO[email=" + dto.email() + ", name=" + dto.name()
                    + ", password=[PROTECTED], confirm_password=[PROTECTED], adminType=" + dto.adminType() + "]";
        }
        if (obj instanceof com.passaaqui.backend.modules.auth.dto.RegisterShopkeeperDTO dto) {
            return "RegisterShopkeeperDTO[email=" + dto.email() + ", name=" + dto.name()
                    + ", password=[PROTECTED], confirm_password=[PROTECTED], documentId=" + dto.documentId()
                    + ", companyName=" + dto.companyName() + ", poiName=" + dto.poiName() + ", cityId=" + dto.cityId() + "]";
        }
        if (obj instanceof com.passaaqui.backend.modules.tourist.dto.UpdateTouristDTO dto) {
            return "UpdateTouristDTO[name=" + dto.name() + ", password=[PROTECTED], documentId=" + dto.documentId() + ", theme=" + dto.theme() + "]";
        }
        if (obj instanceof com.passaaqui.backend.modules.shopkeeper.dto.UpdateShopkeeperDTO dto) {
            return "UpdateShopkeeperDTO[name=" + dto.name() + ", password=[PROTECTED], companyName=" + dto.companyName() + "]";
        }
        return obj;
    }

    private Object maskResult(Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            Object body = responseEntity.getBody();
            if (body instanceof com.passaaqui.backend.shared.objects.JWTObject) {
                return "<" + responseEntity.getStatusCode() + ",JWTObject(access_token=[PROTECTED], refresh_token=[PROTECTED])>";
            }
        }
        return result;
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
