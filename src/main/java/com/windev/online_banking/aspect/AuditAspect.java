package com.windev.online_banking.aspect;

import com.windev.online_banking.entity.User;
import com.windev.online_banking.payload.request.LoginRequest;
import com.windev.online_banking.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    @Autowired
    private UserRepository userRepository;

    @Pointcut("execution(* com.windev.online_banking.controller.AuthController.authenticateUser(..))")
    public void authenticateUser() {}

    @AfterReturning("authenticateUser()")
    public void afterAuthenticateUser(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof LoginRequest) {
            LoginRequest loginRequest = (LoginRequest) args[0];
            log.info("User '{}' attempted to login.", loginRequest.getUsername());
        }
    }

    // Thêm các advice khác cho các phương thức quan trọng
}
