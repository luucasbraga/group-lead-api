package com.grouplead.repository;

import com.grouplead.domain.entity.Commit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommitRepository extends JpaRepository<Commit, Long> {

    Optional<Commit> findBySha(String sha);

    List<Commit> findByDeveloperId(Long developerId);

    @Query("SELECT c FROM Commit c WHERE c.committedAt >= :since ORDER BY c.committedAt DESC")
    List<Commit> findCommittedSince(@Param("since") LocalDateTime since);

    @Query("SELECT c FROM Commit c WHERE c.developer.id = :developerId AND c.committedAt BETWEEN :start AND :end")
    List<Commit> findByDeveloperIdInPeriod(
            @Param("developerId") Long developerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT c FROM Commit c WHERE c.developer.team.id = :teamId AND c.committedAt BETWEEN :start AND :end")
    List<Commit> findByTeamIdInPeriod(
            @Param("teamId") Long teamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(c) FROM Commit c WHERE c.developer.id = :developerId AND c.committedAt BETWEEN :start AND :end")
    long countByDeveloperIdInPeriod(
            @Param("developerId") Long developerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COALESCE(SUM(c.additions), 0) FROM Commit c WHERE c.developer.id = :developerId AND c.committedAt BETWEEN :start AND :end")
    int sumAdditionsByDeveloperIdInPeriod(
            @Param("developerId") Long developerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COALESCE(SUM(c.deletions), 0) FROM Commit c WHERE c.developer.id = :developerId AND c.committedAt BETWEEN :start AND :end")
    int sumDeletionsByDeveloperIdInPeriod(
            @Param("developerId") Long developerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT c FROM Commit c WHERE c.developer.id = :developerId AND " +
            "(HOUR(c.committedAt) < 9 OR HOUR(c.committedAt) >= 20) AND c.committedAt BETWEEN :start AND :end")
    List<Commit> findAfterHoursCommits(
            @Param("developerId") Long developerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    boolean existsBySha(String sha);
}
