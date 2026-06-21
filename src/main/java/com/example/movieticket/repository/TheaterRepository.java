package com.example.movieticket.repository;

import com.example.movieticket.domain.Theater;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TheaterRepository extends JpaRepository<Theater, Long> {

    boolean existsByCityIdAndNameIgnoreCase(Long cityId, String name);

    List<Theater> findByCityIdOrderByNameAsc(Long cityId);
}
