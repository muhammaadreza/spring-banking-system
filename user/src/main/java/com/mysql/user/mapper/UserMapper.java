package com.mysql.user.mapper;

import com.mysql.user.dto.UserCreateDTO;
import com.mysql.user.dto.UserResponseDTO;
import com.mysql.user.dto.UserUpdateDTO;
import com.mysql.user.model.ModelUser;

public class UserMapper {

    public static ModelUser toEntity(UserCreateDTO dto) {
        ModelUser user = new ModelUser();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPhoneNumber(dto.getPhoneNumber());
        return user;
    }

    public static void updateEntityFromDto(UserUpdateDTO dto, ModelUser entity) {
        if (dto.getUsername() != null) {
            entity.setUsername(dto.getUsername());
        }
        if (dto.getPassword() != null) {
            entity.setPassword(dto.getPassword());
        }
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getPhoneNumber() != null) {
            entity.setPhoneNumber(dto.getPhoneNumber());
        }
    }

    public static UserResponseDTO toDTO(ModelUser entity) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setName(entity.getName());
        dto.setPhoneNumber(entity.getPhoneNumber());
        return dto;
    }
}