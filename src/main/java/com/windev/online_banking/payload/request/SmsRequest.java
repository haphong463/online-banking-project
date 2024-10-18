package com.windev.online_banking.payload.request;

import lombok.Data;

@Data
public class SmsRequest {
    private String to;
    private String message;
}
