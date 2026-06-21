package com.example.movieticket.repository;

import com.example.movieticket.domain.RefundPolicy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, Long> {

    boolean existsByHoursBeforeShow(int hoursBeforeShow);

    List<RefundPolicy> findAllByOrderByHoursBeforeShowDesc();
}
