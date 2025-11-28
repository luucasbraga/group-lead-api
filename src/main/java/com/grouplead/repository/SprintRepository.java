package com.grouplead.repository;

import com.grouplead.domain.entity.Sprint;
import com.grouplead.domain.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {

    Optional<Sprint> findByExternalId(String externalId);

    List<Sprint> findByTeamId(Long teamId);

    Page<Sprint> findByTeamIdOrderByStartDateDesc(Long teamId, Pageable pageable);

    Page<Sprint> findAllByOrderByStartDateDesc(Pageable pageable);

    List<Sprint> findTopByTeamIdOrderByEndDateDesc(Long teamId, Pageable pageable);

    List<Sprint> findByTeamIdAndStatusOrderByEndDateDesc(Long teamId, SprintStatus status, Pageable pageable);

    List<Sprint> findByTeamIdOrderByEndDateDesc(Long teamId, Pageable pageable);

    List<Sprint> findByStatus(SprintStatus status);

    @Query("SELECT s FROM Sprint s WHERE s.team.id = :teamId AND s.status = :status")
    List<Sprint> findByTeamIdAndStatus(@Param("teamId") Long teamId, @Param("status") SprintStatus status);

    @Query("SELECT s FROM Sprint s WHERE s.status = 'ACTIVE'")
    List<Sprint> findActiveSprints();

    @Query("SELECT s FROM Sprint s WHERE s.team.id = :teamId AND s.status = 'ACTIVE'")
    Optional<Sprint> findActiveSprintByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT s FROM Sprint s WHERE s.team.id = :teamId ORDER BY s.endDate DESC")
    List<Sprint> findByTeamIdOrderByEndDateDesc(@Param("teamId") Long teamId);

    @Query("SELECT s FROM Sprint s WHERE s.team.id = :teamId ORDER BY s.endDate DESC LIMIT :limit")
    List<Sprint> findLastSprintsByTeamId(@Param("teamId") Long teamId, @Param("limit") int limit);

    @Query("SELECT s FROM Sprint s LEFT JOIN FETCH s.tickets WHERE s.id = :id")
    Optional<Sprint> findByIdWithTickets(@Param("id") Long id);

    @Query("SELECT s FROM Sprint s WHERE s.endDate < :date AND s.status = 'ACTIVE'")
    List<Sprint> findOverdueSprints(@Param("date") LocalDate date);

    boolean existsByExternalId(String externalId);
}
