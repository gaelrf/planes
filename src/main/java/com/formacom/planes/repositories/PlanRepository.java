package com.formacom.planes.repositories;

import com.formacom.planes.models.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findTop5ByOrderByFechaDesc();
}