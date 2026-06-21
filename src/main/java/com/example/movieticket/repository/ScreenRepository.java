package com.example.movieticket.repository;

import com.example.movieticket.domain.Screen;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

    boolean existsByTheaterIdAndNameIgnoreCase(Long theaterId, String name);

    List<Screen> findByTheaterIdOrderByNameAsc(Long theaterId);
}
