package com.grouplead.repository;

import com.grouplead.domain.entity.Metric;
import com.grouplead.domain.enums.MetricType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MetricRepository extends JpaRepository<Metric, Long> {

    List<Metric> findByType(MetricType type);

    List<Metric> findBySource(String source);

    @Query("SELECT m FROM Metric m WHERE m.type = :type AND m.timestamp BETWEEN :start AND :end ORDER BY m.timestamp ASC")
    List<Metric> findByTypeAndPeriod(
            @Param("type") MetricType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT m FROM Metric m WHERE m.source = :source AND m.timestamp BETWEEN :start AND :end ORDER BY m.timestamp ASC")
    List<Metric> findBySourceAndPeriod(
            @Param("source") String source,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT m FROM Metric m WHERE m.type = :type ORDER BY m.timestamp DESC LIMIT 1")
    Optional<Metric> findLatestByType(@Param("type") MetricType type);

    @Query("SELECT m FROM Metric m WHERE m.type = :type AND m.name = :name ORDER BY m.timestamp DESC LIMIT 1")
    Optional<Metric> findLatestByTypeAndName(@Param("type") MetricType type, @Param("name") String name);

    @Query("SELECT AVG(m.value) FROM Metric m WHERE m.type = :type AND m.timestamp BETWEEN :start AND :end")
    Double calculateAverageByTypeInPeriod(
            @Param("type") MetricType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT MAX(m.value) FROM Metric m WHERE m.type = :type AND m.timestamp BETWEEN :start AND :end")
    Double findMaxByTypeInPeriod(
            @Param("type") MetricType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT MIN(m.value) FROM Metric m WHERE m.type = :type AND m.timestamp BETWEEN :start AND :end")
    Double findMinByTypeInPeriod(
            @Param("type") MetricType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT m FROM Metric m WHERE m.timestamp >= :since ORDER BY m.timestamp DESC")
    List<Metric> findRecent(@Param("since") LocalDateTime since);

    @Query("DELETE FROM Metric m WHERE m.timestamp < :before")
    void deleteOlderThan(@Param("before") LocalDateTime before);

    @Query("SELECT AVG(m.value) FROM Metric m WHERE m.team.id = :teamId AND m.type = 'VELOCITY' " +
            "AND m.timestamp BETWEEN :start AND :end")
    Double findAverageVelocityForTeam(@Param("teamId") Long teamId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(c) FROM Commit c WHERE c.developer.id = :developerId " +
            "AND c.timestamp >= :since AND (HOUR(c.timestamp) < 9 OR HOUR(c.timestamp) >= 18)")
    long countAfterHoursCommits(@Param("developerId") Long developerId,
                                 @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(c) FROM Commit c WHERE c.developer.id = :developerId " +
            "AND c.timestamp >= :since")
    long countTotalCommits(@Param("developerId") Long developerId,
                           @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(c) FROM Commit c WHERE c.developer.id = :developerId " +
            "AND c.timestamp >= :since AND (DAYOFWEEK(c.timestamp) = 1 OR DAYOFWEEK(c.timestamp) = 7)")
    long countWeekendCommits(@Param("developerId") Long developerId,
                              @Param("since") LocalDateTime since);
}
