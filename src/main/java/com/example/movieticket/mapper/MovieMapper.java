package com.example.movieticket.mapper;

import com.example.movieticket.domain.Movie;
import com.example.movieticket.web.dto.MovieResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    MovieResponse toResponse(Movie movie);

    List<MovieResponse> toResponseList(List<Movie> movies);
}
