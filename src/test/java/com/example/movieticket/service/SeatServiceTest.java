package com.example.movieticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.movieticket.domain.City;
import com.example.movieticket.domain.Screen;
import com.example.movieticket.domain.Seat;
import com.example.movieticket.domain.Theater;
import com.example.movieticket.domain.enums.SeatCategory;
import com.example.movieticket.exception.DuplicateResourceException;
import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.repository.ScreenRepository;
import com.example.movieticket.repository.SeatRepository;
import com.example.movieticket.support.factory.CityFactory;
import com.example.movieticket.support.factory.ScreenFactory;
import com.example.movieticket.support.factory.TheaterFactory;
import com.example.movieticket.web.dto.SeatLayoutResponse;
import com.example.movieticket.web.dto.SeatRowSpec;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;
    @Mock
    private ScreenRepository screenRepository;

    @InjectMocks
    private SeatService seatService;

    @Captor
    private ArgumentCaptor<List<Seat>> seatsCaptor;

    private final Theater theater = TheaterFactory.withId(5L,
            TheaterFactory.theater(CityFactory.withId(3L, CityFactory.city("Bengaluru")), "PVR Forum"));
    private final Screen screen = ScreenFactory.withId(7L, ScreenFactory.screen(theater, "Audi 1"));

    private SeatRowSpec row(String label, int count, SeatCategory category) {
        SeatRowSpec spec = new SeatRowSpec();
        spec.setRowLabel(label);
        spec.setSeatCount(count);
        spec.setCategory(category);
        return spec;
    }

    @Test
    void defineLayoutGeneratesSeatsPerRow() {
        when(screenRepository.findById(7L)).thenReturn(Optional.of(screen));
        when(seatRepository.existsByScreenId(7L)).thenReturn(false);

        SeatLayoutResponse response = seatService.defineLayout(7L,
                List.of(row("A", 3, SeatCategory.REGULAR), row("B", 2, SeatCategory.PREMIUM)));

        assertThat(response.getTotalSeats()).isEqualTo(5);
        org.mockito.Mockito.verify(seatRepository).saveAll(seatsCaptor.capture());
        assertThat(seatsCaptor.getValue()).hasSize(5);
    }

    @Test
    void defineLayoutThrowsWhenScreenMissing() {
        when(screenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatService.defineLayout(99L, List.of(row("A", 1, SeatCategory.REGULAR))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void defineLayoutThrowsWhenLayoutAlreadyExists() {
        when(screenRepository.findById(7L)).thenReturn(Optional.of(screen));
        when(seatRepository.existsByScreenId(7L)).thenReturn(true);

        assertThatThrownBy(() -> seatService.defineLayout(7L, List.of(row("A", 1, SeatCategory.REGULAR))))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
