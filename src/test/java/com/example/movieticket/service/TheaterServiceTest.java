package com.example.movieticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.movieticket.domain.City;
import com.example.movieticket.domain.Theater;
import com.example.movieticket.exception.DuplicateResourceException;
import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.repository.CityRepository;
import com.example.movieticket.repository.TheaterRepository;
import com.example.movieticket.support.factory.CityFactory;
import com.example.movieticket.support.factory.TheaterFactory;
import com.example.movieticket.web.dto.TheaterResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TheaterServiceTest {

    @Mock
    private TheaterRepository theaterRepository;
    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private TheaterService theaterService;

    private final City city = CityFactory.withId(3L, CityFactory.city("Bengaluru"));

    @Test
    void createSavesTrimmedTheaterWhenCityExists() {
        when(cityRepository.findById(3L)).thenReturn(Optional.of(city));
        when(theaterRepository.existsByCityIdAndNameIgnoreCase(3L, "PVR Forum")).thenReturn(false);
        when(theaterRepository.save(any(Theater.class)))
                .thenAnswer(inv -> TheaterFactory.withId(10L, inv.getArgument(0)));

        TheaterResponse response = theaterService.create(3L, "  PVR Forum  ", "  Hosur Road  ");

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getCityId()).isEqualTo(3L);
        assertThat(response.getCityName()).isEqualTo("Bengaluru");
        assertThat(response.getName()).isEqualTo("PVR Forum");
        assertThat(response.getAddress()).isEqualTo("Hosur Road");
    }

    @Test
    void createThrowsWhenCityMissing() {
        when(cityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> theaterService.create(99L, "PVR Forum", "Hosur Road"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createThrowsOnDuplicateNameInCity() {
        when(cityRepository.findById(3L)).thenReturn(Optional.of(city));
        when(theaterRepository.existsByCityIdAndNameIgnoreCase(3L, "PVR Forum")).thenReturn(true);

        assertThatThrownBy(() -> theaterService.create(3L, "PVR Forum", "Hosur Road"))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void listByCityReturnsMappedTheaters() {
        Theater theater = TheaterFactory.withId(10L, TheaterFactory.theater(city, "PVR Forum"));
        when(theaterRepository.findByCityIdOrderByNameAsc(3L)).thenReturn(List.of(theater));

        List<TheaterResponse> result = theaterService.list(3L);

        assertThat(result).singleElement()
                .satisfies(t -> {
                    assertThat(t.getName()).isEqualTo("PVR Forum");
                    assertThat(t.getCityName()).isEqualTo("Bengaluru");
                });
    }
}
