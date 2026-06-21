package com.example.movieticket.support.factory;

import com.example.movieticket.domain.Movie;

public final class MovieFactory {

    private MovieFactory() {
    }

    public static Movie movie(String title, String language, int durationMinutes) {
        return new Movie(title, language, durationMinutes, "UA");
    }

    public static Movie withId(Long id, Movie movie) {
        movie.setId(id);
        return movie;
    }
}
