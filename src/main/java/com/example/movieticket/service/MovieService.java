package com.example.movieticket.service;

import com.example.movieticket.domain.Movie;
import com.example.movieticket.exception.DuplicateResourceException;
import com.example.movieticket.mapper.MovieMapper;
import com.example.movieticket.repository.MovieRepository;
import com.example.movieticket.web.dto.MovieResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    @Transactional
    public MovieResponse create(String title, String language, int durationMinutes, String certification) {
        String trimmedTitle = title.trim();
        String trimmedLanguage = language.trim();
        if (movieRepository.existsByTitleIgnoreCaseAndLanguageIgnoreCase(trimmedTitle, trimmedLanguage)) {
            throw new DuplicateResourceException(
                    "Movie '" + trimmedTitle + "' (" + trimmedLanguage + ") already exists");
        }
        Movie saved = movieRepository.save(new Movie(trimmedTitle, trimmedLanguage, durationMinutes,
                certification == null ? null : certification.trim()));
        log.info("Created movie id={} title={}", saved.getId(), saved.getTitle());
        return movieMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> list() {
        return movieMapper.toResponseList(movieRepository.findAll(Sort.by("title")));
    }
}
