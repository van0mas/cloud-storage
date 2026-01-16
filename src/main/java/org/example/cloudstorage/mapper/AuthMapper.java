package org.example.cloudstorage.mapper;

import org.example.cloudstorage.dto.user.AuthRequestDto;
import org.example.cloudstorage.dto.user.AuthResponseDto;
import org.example.cloudstorage.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    User toEntity(AuthRequestDto dto);

    AuthResponseDto toResponseDto(User user);
}
