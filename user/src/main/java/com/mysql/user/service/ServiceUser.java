package com.mysql.user.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysql.user.dto.UserCreateDTO;
import com.mysql.user.dto.UserUpdateDTO;
import com.mysql.user.mapper.UserMapper;
import com.mysql.user.model.ModelUser;
import com.mysql.user.repository.RepositoryUser;
import com.mysql.user.security.JwtUtil;

@Service
public class ServiceUser {

    @Autowired
    private RepositoryUser repositoryUser;

    @Autowired
    private JwtUtil jwtUtil;

    // Login logic
    public Map<String, Object> loginUser(String username, String password) {
        Map<String, Object> response = new HashMap<>();
        ModelUser user = repositoryUser.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            String token = jwtUtil.generateToken(user.getId());
            response.put("success", true);
            response.put("token", token);
            response.put("message", "Login successful");
        } else {
            response.put("success", false);
            response.put("message", "Invalid credentials");
        }
        return response;
    }

    // Register logic
    public Map<String, Object> registerUser(UserCreateDTO userCreateDTO) {
        Map<String, Object> response = new HashMap<>();
        ModelUser existingUser = repositoryUser.findByUsername(userCreateDTO.getUsername());
        if (existingUser != null) {
            response.put("success", false);
            response.put("message", "Username already exists");
            return response;
        }

        ModelUser user = UserMapper.toEntity(userCreateDTO);
        ModelUser savedUser = repositoryUser.save(user);
        response.put("success", true);
        response.put("data", UserMapper.toDTO(savedUser));
        response.put("message", "User registered successfully");
        return response;
    }

    // Get user profile
    public Map<String, Object> getMyProfile(int userId) {
        Map<String, Object> response = new HashMap<>();
        Optional<ModelUser> userOpt = repositoryUser.findById(userId);

        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }

        response.put("success", true);
        response.put("data", UserMapper.toDTO(userOpt.get()));
        response.put("message", "Profile retrieved successfully");
        return response;
    }

    // Update user profile
    public Map<String, Object> updateMyProfile(int userId, UserUpdateDTO updatedUserDTO) {
        Map<String, Object> response = new HashMap<>();
        Optional<ModelUser> currentUserOpt = repositoryUser.findById(userId);

        if (currentUserOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }

        ModelUser currentUser = currentUserOpt.get();
        UserMapper.updateEntityFromDto(updatedUserDTO, currentUser);
        ModelUser savedUser = repositoryUser.save(currentUser);

        response.put("success", true);
        response.put("data", UserMapper.toDTO(savedUser));
        response.put("message", "Profile updated successfully");
        return response;
    }

    // Delete user account
    public Map<String, Object> deleteMyAccount(int userId) {
        Map<String, Object> response = new HashMap<>();
        Optional<ModelUser> currentUserOpt = repositoryUser.findById(userId);

        if (currentUserOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }

        repositoryUser.delete(currentUserOpt.get());
        response.put("success", true);
        response.put("message", "Account deleted successfully");
        return response;
    }
}