package com.example.movieticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.movieticket.domain.City;
import com.example.movieticket.domain.Screen;
import com.example.movieticket.domain.Theater;
import com.example.movieticket.exception.DuplicateResourceException;
import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.mapper.ScreenMapperImpl;
import com.example.movieticket.repository.ScreenRepository;
import com.example.movieticket.repository.TheaterRepository;
import com.example.movieticket.support.factory.CityFactory;
import com.example.movieticket.support.factory.ScreenFactory;
import com.example.movieticket.support.factory.TheaterFactory;
import com.example.movieticket.web.dto.ScreenResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScreenServiceTest {

    @Mock
    private ScreenRepository screenRepository;
    @Mock
    private TheaterRepository theaterRepository;

    private ScreenService screenService;

    @BeforeEach
    void setUp() {
        screenService = new ScreenService(screenRepository, theaterRepository, new ScreenMapperImpl());
    }

    private final City city = CityFactory.withId(3L, CityFactory.city("Bengaluru"));
    private final Theater theater = TheaterFactory.withId(5L, TheaterFactory.theater(city, "PVR Forum"));

    @Test
    void createSavesTrimmedScreenWhenTheaterExists() {
        when(theaterRepository.findById(5L)).thenReturn(Optional.of(theater));
        when(screenRepository.existsByTheaterIdAndNameIgnoreCase(5L, "Audi 1")).thenReturn(false);
        when(screenRepository.save(any(Screen.class)))
                .thenAnswer(inv -> ScreenFactory.withId(20L, inv.getArgument(0)));

        ScreenResponse response = screenService.create(5L, "  Audi 1  ");

        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getTheaterId()).isEqualTo(5L);
        assertThat(response.getTheaterName()).isEqualTo("PVR Forum");
        assertThat(response.getName()).isEqualTo("Audi 1");
    }

    @Test
    void createThrowsWhenTheaterMissing() {
        when(theaterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screenService.create(99L, "Audi 1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createThrowsOnDuplicateNameInTheater() {
        when(theaterRepository.findById(5L)).thenReturn(Optional.of(theater));
        when(screenRepository.existsByTheaterIdAndNameIgnoreCase(5L, "Audi 1")).thenReturn(true);

        assertThatThrownBy(() -> screenService.create(5L, "Audi 1"))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void listByTheaterReturnsMappedScreens() {
        Screen screen = ScreenFactory.withId(20L, ScreenFactory.screen(theater, "Audi 1"));
        when(screenRepository.findByTheaterIdOrderByNameAsc(5L)).thenReturn(List.of(screen));

        List<ScreenResponse> result = screenService.listByTheater(5L);

        assertThat(result).singleElement()
                .satisfies(s -> {
                    assertThat(s.getName()).isEqualTo("Audi 1");
                    assertThat(s.getTheaterName()).isEqualTo("PVR Forum");
                });
    }
}
