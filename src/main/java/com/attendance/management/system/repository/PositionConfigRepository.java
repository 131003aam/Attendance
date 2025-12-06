package com.attendance.management.system.repository;

import com.attendance.management.system.entity.PositionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PositionConfigRepository extends JpaRepository<PositionConfig, Long> {
    Optional<PositionConfig> findByPositionId(String positionId);
}
