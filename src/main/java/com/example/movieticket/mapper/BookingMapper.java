package com.example.movieticket.mapper;

import com.example.movieticket.domain.Booking;
import com.example.movieticket.domain.BookingSeat;
import com.example.movieticket.web.dto.BookedSeat;
import com.example.movieticket.web.dto.BookingSummaryResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "showSeatId", source = "showSeat.id")
    @Mapping(target = "rowLabel", source = "showSeat.seat.rowLabel")
    @Mapping(target = "seatNumber", source = "showSeat.seat.seatNumber")
    BookedSeat toBookedSeat(BookingSeat bookingSeat);

    List<BookedSeat> toBookedSeats(List<BookingSeat> bookingSeats);

    @Mapping(target = "bookingId", source = "id")
    @Mapping(target = "showId", source = "show.id")
    BookingSummaryResponse toSummary(Booking booking);

    List<BookingSummaryResponse> toSummaryList(List<Booking> bookings);
}
