package com.example.movieticket.mapper;

import com.example.movieticket.domain.Screen;
import com.example.movieticket.web.dto.ScreenResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScreenMapper {

    @Mapping(target = "theaterId", source = "theater.id")
    @Mapping(target = "theaterName", source = "theater.name")
    ScreenResponse toResponse(Screen screen);

    List<ScreenResponse> toResponseList(List<Screen> screens);
}
