package com.example.movieticket.repository;

import com.example.movieticket.domain.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {

    boolean existsByNameIgnoreCase(String name);
}
