package com.example.movieticket.mapper;

import com.example.movieticket.domain.Show;
import com.example.movieticket.web.dto.ShowResponse;
import com.example.movieticket.web.dto.ShowSummaryResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShowMapper {

    @Mapping(target = "screenId", source = "show.screen.id")
    @Mapping(target = "screenName", source = "show.screen.name")
    @Mapping(target = "movieId", source = "show.movie.id")
    @Mapping(target = "movieTitle", source = "show.movie.title")
    @Mapping(target = "totalSeats", source = "totalSeats")
    ShowResponse toResponse(Show show, int totalSeats);

    @Mapping(target = "movieId", source = "movie.id")
    @Mapping(target = "movieTitle", source = "movie.title")
    @Mapping(target = "screenId", source = "screen.id")
    @Mapping(target = "screenName", source = "screen.name")
    @Mapping(target = "theaterName", source = "screen.theater.name")
    @Mapping(target = "cityName", source = "screen.theater.city.name")
    ShowSummaryResponse toSummary(Show show);

    List<ShowSummaryResponse> toSummaryList(List<Show> shows);
}
