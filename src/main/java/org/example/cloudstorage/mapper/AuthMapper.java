package org.example.cloudstorage.mapper;

import org.example.cloudstorage.dto.AuthRequestDto;
import org.example.cloudstorage.dto.AuthResponseDto;
import org.example.cloudstorage.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    User toEntity(AuthRequestDto dto);

    AuthResponseDto toResponseDto(User user);
}
