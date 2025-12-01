package org.example.cloudstorage.mapper;

import org.example.cloudstorage.dto.AuthRequestDto;
import org.example.cloudstorage.dto.AuthResponseDto;
import org.example.cloudstorage.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    User toEntity(AuthRequestDto dto);

    @Mapping(target = "username", source = "username")
    AuthResponseDto toResponseDto(User user);
}
