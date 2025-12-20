package com.mongodb.investment.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ServiceTransaction {

    @Autowired
    private RestTemplate restTemplate;

    private final String TRANSACTION_SERVICE_URL = "http://localhost:68/transactions";

    public Map<String, Object> createTransaction(Map<String, Object> transactionData, String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(transactionData, headers);

            String endpoint = "";
            String type = (String) transactionData.get("type");

            if ("deposito".equals(type)) {
                endpoint = "/deposito";
            } else if ("investment".equals(type)) {
                endpoint = "/investment";
            } else {
                response.put("success", false);
                response.put("message", "Unsupported transaction type for this service.");
                return response;
            }

            ResponseEntity<String> restResponse = restTemplate.exchange(
                    TRANSACTION_SERVICE_URL + endpoint,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.readValue(restResponse.getBody(), Map.class);

            response.put("success", true);
            response.put("transaction", result);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create transaction record: " + e.getMessage());
        }

        return response;
    }

    public Map<String, Object> createInvestmentTransaction(Map<String, Object> transactionData, String token) {
        transactionData.put("type", "investment");
        return createTransaction(transactionData, token);
    }

    public Map<String, Object> createDepositoTransaction(Map<String, Object> transactionData, String token) {
        transactionData.put("type", "deposito");
        return createTransaction(transactionData, token);
    }
}