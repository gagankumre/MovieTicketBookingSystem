package com.example.movieticket.mapper;

import com.example.movieticket.domain.City;
import com.example.movieticket.web.dto.CityResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CityMapper {

    CityResponse toResponse(City city);

    List<CityResponse> toResponseList(List<City> cities);
}
