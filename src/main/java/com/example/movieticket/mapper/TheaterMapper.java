package com.example.movieticket.mapper;

import com.example.movieticket.domain.Theater;
import com.example.movieticket.web.dto.TheaterResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TheaterMapper {

    @Mapping(target = "cityId", source = "city.id")
    @Mapping(target = "cityName", source = "city.name")
    TheaterResponse toResponse(Theater theater);

    List<TheaterResponse> toResponseList(List<Theater> theaters);
}
