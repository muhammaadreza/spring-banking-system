// ControllerAuth.java
package com.mysql.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mysql.user.dto.UserCreateDTO;
import com.mysql.user.model.ModelUser;
import com.mysql.user.service.ServiceUser;

import java.util.Map;

@RestController
public class ControllerAuth {

    @Autowired
    private ServiceUser serviceUser;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody ModelUser user) {
        Map<String, Object> result = serviceUser.loginUser(user.getUsername(), user.getPassword());
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserCreateDTO userCreateDTO) {
        Map<String, Object> result = serviceUser.registerUser(userCreateDTO);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}