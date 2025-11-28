package com.grouplead.repository;

import com.grouplead.domain.entity.Alert;
import com.grouplead.domain.enums.AlertSeverity;
import com.grouplead.domain.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    Page<Alert> findByTeamIdOrderByCreatedAtDesc(Long teamId, Pageable pageable);

    Page<Alert> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Alert> findByTeamIdAndResolvedFalseOrderByCreatedAtDesc(Long teamId);

    List<Alert> findByResolvedFalseOrderByCreatedAtDesc();

    List<Alert> findByType(AlertType type);

    List<Alert> findBySeverity(AlertSeverity severity);

    @Query("SELECT a FROM Alert a WHERE a.resolvedAt IS NULL ORDER BY a.severity DESC, a.createdAt DESC")
    List<Alert> findActiveAlerts();

    @Query("SELECT a FROM Alert a WHERE a.resolvedAt IS NULL AND a.acknowledged = false ORDER BY a.severity DESC, a.createdAt DESC")
    List<Alert> findUnacknowledgedAlerts();

    @Query("SELECT a FROM Alert a WHERE a.createdAt BETWEEN :start AND :end ORDER BY a.createdAt DESC")
    List<Alert> findByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Alert a WHERE a.severity = :severity AND a.resolvedAt IS NULL")
    List<Alert> findActiveBySeverity(@Param("severity") AlertSeverity severity);

    @Query("SELECT a FROM Alert a WHERE a.type = :type AND a.resolvedAt IS NULL")
    List<Alert> findActiveByType(@Param("type") AlertType type);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.resolvedAt IS NULL")
    long countActiveAlerts();

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.severity = :severity AND a.resolvedAt IS NULL")
    long countActiveBySeverity(@Param("severity") AlertSeverity severity);

    @Query("SELECT a FROM Alert a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<Alert> findRecentAlerts(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.createdAt BETWEEN :start AND :end")
    long countAlertsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
