package com.grouplead.repository;

import com.grouplead.domain.entity.Deployment;
import com.grouplead.domain.enums.DeploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, Long> {

    List<Deployment> findByEnvironment(String environment);

    List<Deployment> findByStatus(DeploymentStatus status);

    @Query("SELECT d FROM Deployment d WHERE d.deployedAt BETWEEN :start AND :end")
    List<Deployment> findByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT d FROM Deployment d WHERE d.environment = :environment AND d.deployedAt BETWEEN :start AND :end")
    List<Deployment> findByEnvironmentAndPeriod(
            @Param("environment") String environment,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT d FROM Deployment d WHERE d.causedIncident = true AND d.deployedAt BETWEEN :start AND :end")
    List<Deployment> findFailedDeploymentsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(d) FROM Deployment d WHERE d.deployedAt BETWEEN :start AND :end")
    long countDeploymentsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(d) FROM Deployment d WHERE d.causedIncident = true AND d.deployedAt BETWEEN :start AND :end")
    long countFailedDeploymentsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(d) FROM Deployment d WHERE d.environment = :environment AND d.deployedAt >= :since")
    long countDeploymentsByEnvironmentSince(@Param("environment") String environment, @Param("since") LocalDateTime since);

    @Query("SELECT d FROM Deployment d WHERE d.deployedAt >= :since ORDER BY d.deployedAt DESC")
    List<Deployment> findRecentDeployments(@Param("since") LocalDateTime since);
}
