package com.passaaqui.backend.unit.logging;

import com.passaaqui.backend.infra.logging.LoggingAspect;
import com.passaaqui.backend.modules.auth.dto.LoginDTO;
import com.passaaqui.backend.modules.auth.dto.RegisterTouristDTO;
import com.passaaqui.backend.shared.objects.JWTObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @InjectMocks
    private LoggingAspect loggingAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Test
    void logExecution_shouldProceedAndMaskSensitiveArguments() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.toShortString()).thenReturn("AuthService.loginAccount(..)");
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"email", "password", "userAgent", "ipAddress"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{"test@test.com", "PlainPassword123!", "Mozilla/5.0", "127.0.0.1"});

        JWTObject jwt = new JWTObject();
        jwt.setAccess_token("access-token-xyz");
        jwt.setRefresh_token("refresh-token-xyz");
        when(joinPoint.proceed()).thenReturn(jwt);

        Object result = loggingAspect.logExecution(joinPoint);

        assertSame(jwt, result);
        assertEquals("JWTObject(access_token=[PROTECTED], refresh_token=[PROTECTED])", jwt.toString());
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void logExecution_shouldMaskLoginDTOAndResponseEntity() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.toShortString()).thenReturn("AuthController.login(..)");
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"dto", "request"});

        LoginDTO loginDTO = new LoginDTO("test@test.com", "SecretPass123!");
        when(joinPoint.getArgs()).thenReturn(new Object[]{loginDTO, null});

        JWTObject jwt = new JWTObject();
        jwt.setAccess_token("access-token");
        jwt.setRefresh_token("refresh-token");
        ResponseEntity<JWTObject> responseEntity = ResponseEntity.ok(jwt);
        when(joinPoint.proceed()).thenReturn(responseEntity);

        Object result = loggingAspect.logExecution(joinPoint);

        assertSame(responseEntity, result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void logExecution_shouldMaskRegisterTouristDTO() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.toShortString()).thenReturn("AuthController.registerAccountTourist(..)");
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"dto"});

        RegisterTouristDTO dto = new RegisterTouristDTO("test@test.com", "Tourist", "Pass123!", "Pass123!", "52998224725");
        when(joinPoint.getArgs()).thenReturn(new Object[]{dto});
        when(joinPoint.proceed()).thenReturn("created");

        Object result = loggingAspect.logExecution(joinPoint);

        assertEquals("created", result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void logExecution_shouldPropagateException() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.toShortString()).thenReturn("AuthService.loginAccount(..)");
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"email"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{"test@test.com"});
        when(joinPoint.proceed()).thenThrow(new IllegalArgumentException("Invalid state"));

        assertThrows(IllegalArgumentException.class, () -> loggingAspect.logExecution(joinPoint));
        verify(joinPoint, times(1)).proceed();
    }
}
