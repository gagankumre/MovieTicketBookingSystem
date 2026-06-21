package com.example.movieticket.support.factory;

import com.example.movieticket.domain.City;

public final class CityFactory {

    private CityFactory() {
    }

    public static City city(String name) {
        return new City(name);
    }

    public static City withId(Long id, City city) {
        city.setId(id);
        return city;
    }
}
