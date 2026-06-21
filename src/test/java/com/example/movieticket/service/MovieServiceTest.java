package com.example.movieticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.movieticket.domain.Movie;
import com.example.movieticket.exception.DuplicateResourceException;
import com.example.movieticket.repository.MovieRepository;
import com.example.movieticket.support.factory.MovieFactory;
import com.example.movieticket.web.dto.MovieResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    @Test
    void createSavesTrimmedMovie() {
        when(movieRepository.existsByTitleIgnoreCaseAndLanguageIgnoreCase("Inception", "English"))
                .thenReturn(false);
        when(movieRepository.save(any(Movie.class)))
                .thenAnswer(inv -> MovieFactory.withId(7L, inv.getArgument(0)));

        MovieResponse response = movieService.create("  Inception  ", "  English  ", 148, "UA");

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getTitle()).isEqualTo("Inception");
        assertThat(response.getLanguage()).isEqualTo("English");
        assertThat(response.getDurationMinutes()).isEqualTo(148);
    }

    @Test
    void createRejectsDuplicateTitleAndLanguage() {
        when(movieRepository.existsByTitleIgnoreCaseAndLanguageIgnoreCase("Inception", "English"))
                .thenReturn(true);

        assertThatThrownBy(() -> movieService.create("Inception", "English", 148, "UA"))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
