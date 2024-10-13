package com.windev.online_banking.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BankingException extends RuntimeException {
    private HttpStatus status;

    public BankingException(String message, HttpStatus status){
        super(message);
        this.status = status;
    }
}