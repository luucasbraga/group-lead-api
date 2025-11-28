package com.grouplead.repository;

import com.grouplead.domain.entity.MergeRequest;
import com.grouplead.domain.enums.MergeRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MergeRequestRepository extends JpaRepository<MergeRequest, Long> {

    Optional<MergeRequest> findByExternalIdAndProjectId(String externalId, String projectId);

    List<MergeRequest> findByDeveloperId(Long developerId);

    List<MergeRequest> findByStatus(MergeRequestStatus status);

    @Query("SELECT mr FROM MergeRequest mr WHERE mr.mergedAt BETWEEN :start AND :end")
    List<MergeRequest> findMergedInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT mr FROM MergeRequest mr WHERE mr.developer.id = :developerId AND mr.mergedAt BETWEEN :start AND :end")
    List<MergeRequest> findMergedByDeveloperInPeriod(
            @Param("developerId") Long developerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT mr FROM MergeRequest mr WHERE mr.developer.team.id = :teamId AND mr.mergedAt BETWEEN :start AND :end")
    List<MergeRequest> findMergedByTeamInPeriod(
            @Param("teamId") Long teamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT mr FROM MergeRequest mr WHERE mr.status = 'OPEN' ORDER BY mr.createdAt ASC")
    List<MergeRequest> findOpenMergeRequests();

    @Query("SELECT mr FROM MergeRequest mr WHERE mr.developer.id = :developerId AND mr.status = 'OPEN'")
    List<MergeRequest> findOpenByDeveloperId(@Param("developerId") Long developerId);

    @Query("SELECT COUNT(mr) FROM MergeRequest mr WHERE mr.developer.id = :developerId AND mr.mergedAt BETWEEN :start AND :end")
    long countMergedByDeveloperInPeriod(
            @Param("developerId") Long developerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, mr.createdAt, mr.mergedAt)) FROM MergeRequest mr " +
            "WHERE mr.developer.team.id = :teamId AND mr.mergedAt BETWEEN :start AND :end AND mr.mergedAt IS NOT NULL")
    Double calculateAverageReviewTimeByTeam(
            @Param("teamId") Long teamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    boolean existsByExternalIdAndProjectId(String externalId, String projectId);
}
