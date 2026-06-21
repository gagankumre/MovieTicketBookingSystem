package com.example.movieticket.mapper;

import com.example.movieticket.domain.User;
import com.example.movieticket.web.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
