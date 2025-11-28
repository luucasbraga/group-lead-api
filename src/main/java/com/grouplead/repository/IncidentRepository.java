package com.grouplead.repository;

import com.grouplead.domain.entity.Incident;
import com.grouplead.domain.enums.IncidentSeverity;
import com.grouplead.domain.enums.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Page<Incident> findByTeamIdOrderByStartedAtDesc(Long teamId, Pageable pageable);

    Page<Incident> findAllByOrderByStartedAtDesc(Pageable pageable);

    List<Incident> findByTeamIdAndStatusNotOrderByStartedAtDesc(Long teamId, IncidentStatus status);

    List<Incident> findByStatusNotOrderByStartedAtDesc(IncidentStatus status);

    List<Incident> findByTeamIdAndStartedAtBetween(Long teamId, LocalDateTime start, LocalDateTime end);

    List<Incident> findByStartedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Incident> findByTeamIdAndStatusAndResolvedAtBetween(Long teamId, IncidentStatus status,
                                                             LocalDateTime start, LocalDateTime end);

    List<Incident> findByStatusAndResolvedAtBetween(IncidentStatus status, LocalDateTime start, LocalDateTime end);

    List<Incident> findByStatus(IncidentStatus status);

    List<Incident> findBySeverity(IncidentSeverity severity);

    @Query("SELECT i FROM Incident i WHERE i.status != 'RESOLVED' ORDER BY i.severity DESC, i.createdAt ASC")
    List<Incident> findOpenIncidents();

    @Query("SELECT i FROM Incident i WHERE i.resolvedAt BETWEEN :start AND :end")
    List<Incident> findResolvedInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT i FROM Incident i WHERE i.createdAt BETWEEN :start AND :end")
    List<Incident> findCreatedInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, i.createdAt, i.resolvedAt)) FROM Incident i " +
            "WHERE i.resolvedAt BETWEEN :start AND :end AND i.resolvedAt IS NOT NULL")
    Double calculateAverageRecoveryTimeInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.createdAt BETWEEN :start AND :end")
    long countIncidentsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.severity = :severity AND i.createdAt BETWEEN :start AND :end")
    long countBySeverityInPeriod(
            @Param("severity") IncidentSeverity severity,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
