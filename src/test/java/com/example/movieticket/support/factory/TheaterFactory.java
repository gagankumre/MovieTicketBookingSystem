package com.example.movieticket.support.factory;

import com.example.movieticket.domain.City;
import com.example.movieticket.domain.Theater;

public final class TheaterFactory {

    public static final String DEFAULT_ADDRESS = "1 Main Road";

    private TheaterFactory() {
    }

    public static Theater theater(City city, String name) {
        return new Theater(city, name, DEFAULT_ADDRESS);
    }

    public static Theater withId(Long id, Theater theater) {
        theater.setId(id);
        return theater;
    }
}
