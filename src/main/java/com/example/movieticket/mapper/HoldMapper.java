package com.example.movieticket.mapper;

import com.example.movieticket.domain.ShowSeat;
import com.example.movieticket.web.dto.HeldSeat;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HoldMapper {

    @Mapping(target = "showSeatId", source = "id")
    @Mapping(target = "rowLabel", source = "seat.rowLabel")
    @Mapping(target = "seatNumber", source = "seat.seatNumber")
    HeldSeat toHeldSeat(ShowSeat showSeat);

    List<HeldSeat> toHeldSeats(List<ShowSeat> showSeats);
}
