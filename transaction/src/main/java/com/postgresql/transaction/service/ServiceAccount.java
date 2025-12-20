package com.postgresql.transaction.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ServiceAccount {

    @Autowired
    private RestTemplate restTemplate;

    private final String ACCOUNT_SERVICE_URL = "http://localhost:67/accounts";

    // Account Validation by calling User Service
    public boolean isAccountOwnedByUser(int accountId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ACCOUNT_SERVICE_URL,
                    HttpMethod.GET,
                    entity,
                    String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            JsonNode accounts = root.path("data");
            if (accounts.isArray()) {
                for (JsonNode account : accounts) {
                    if (account.path("id").asInt() == accountId) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update Balance Account by calling User Service
    public boolean updateAccountBalance(int accountId, double newBalance, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("balance", newBalance);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Boolean> response = restTemplate.exchange(
                    ACCOUNT_SERVICE_URL + "/update/" + accountId,
                    HttpMethod.PUT,
                    entity,
                    Boolean.class);

            return Boolean.TRUE.equals(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get Balance Account by calling User Service
    public double getAccountBalance(int accountId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ACCOUNT_SERVICE_URL,
                    HttpMethod.GET,
                    entity,
                    String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode accounts = root.path("data");

            if (accounts.isArray()) {
                for (JsonNode account : accounts) {
                    if (account.path("id").asInt() == accountId) {
                        return account.path("balance").asDouble();
                    }
                }
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Get All User accountId by calling User Service
    public List<Integer> getUserAccountId(String token) {
        List<Integer> accountIds = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ACCOUNT_SERVICE_URL,
                    HttpMethod.GET,
                    entity,
                    String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode accounts = root.path("data");

            if (accounts.isArray()) {
                for (JsonNode account : accounts) {
                    accountIds.add(account.path("id").asInt());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accountIds;
    }
}