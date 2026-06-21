package com.example.movieticket.repository;

import com.example.movieticket.domain.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByTitleIgnoreCaseAndLanguageIgnoreCase(String title, String language);
}
