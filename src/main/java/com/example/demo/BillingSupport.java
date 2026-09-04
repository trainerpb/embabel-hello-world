package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BillingSupport implements BaseSupport{
    @Override
    public String support(String userQuery) {
       log.info("BillingSupport: Received user query: {}", userQuery);
        return "Support ticket closed for query: " + userQuery;
    }
}
