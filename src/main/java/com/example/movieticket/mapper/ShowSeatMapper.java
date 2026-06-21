package com.example.movieticket.mapper;

import com.example.movieticket.domain.ShowSeat;
import com.example.movieticket.web.dto.SeatView;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShowSeatMapper {

    @Mapping(target = "showSeatId", source = "id")
    @Mapping(target = "rowLabel", source = "seat.rowLabel")
    @Mapping(target = "seatNumber", source = "seat.seatNumber")
    @Mapping(target = "category", source = "seat.category")
    SeatView toSeatView(ShowSeat showSeat);

    List<SeatView> toSeatViewList(List<ShowSeat> showSeats);
}
