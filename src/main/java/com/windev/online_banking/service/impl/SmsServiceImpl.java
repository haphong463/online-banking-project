package com.windev.online_banking.service.impl;

import com.windev.online_banking.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmsServiceImpl implements SmsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Value("${stringee.appId}")
    private String appId;

    @Value("${stringee.appSecret}")
    private String appSecret;

    @Value("${stringee.fromNumber}")
    private String fromNumber;

    private final RestTemplate restTemplate;

    public SmsServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void sendSms(String to, String message) {
        String url = "https://api.stringee.com/v1/sms";

        // Tạo header với Authorization
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = appId + ":" + appSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        // Tạo body cho yêu cầu
        Map<String, Object> body = new HashMap<>();
        body.put("from", fromNumber);
        body.put("to", to);
        body.put("body", message);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Gửi POST request
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );


        LOGGER.info("-> STATUS CODE: {}", response.getStatusCode());

        // Kiểm tra phản hồi
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to send SMS: " + response.getBody());
        }
    }
}
