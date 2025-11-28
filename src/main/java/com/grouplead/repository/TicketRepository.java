package com.grouplead.repository;

import com.grouplead.domain.entity.Ticket;
import com.grouplead.domain.enums.TicketSource;
import com.grouplead.domain.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByExternalIdAndSource(String externalId, TicketSource source);

    List<Ticket> findBySprintId(Long sprintId);

    List<Ticket> findByDeveloperId(Long developerId);

    List<Ticket> findByStatus(TicketStatus status);

    @Query("SELECT t FROM Ticket t WHERE t.sprint.id = :sprintId AND t.status = :status")
    List<Ticket> findBySprintIdAndStatus(@Param("sprintId") Long sprintId, @Param("status") TicketStatus status);

    @Query("SELECT t FROM Ticket t WHERE t.developer.id = :developerId AND t.status = :status")
    List<Ticket> findByDeveloperIdAndStatus(@Param("developerId") Long developerId, @Param("status") TicketStatus status);

    @Query("SELECT t FROM Ticket t WHERE t.createdAt >= :since ORDER BY t.createdAt DESC")
    List<Ticket> findCreatedSince(@Param("since") LocalDateTime since);

    @Query("SELECT t FROM Ticket t WHERE t.completedAt >= :since ORDER BY t.completedAt DESC")
    List<Ticket> findCompletedSince(@Param("since") LocalDateTime since);

    @Query("SELECT t FROM Ticket t WHERE t.externalUpdatedAt >= :since")
    List<Ticket> findUpdatedSince(@Param("since") LocalDateTime since);

    @Query("SELECT t FROM Ticket t WHERE t.developer.id = :developerId AND t.completedAt BETWEEN :start AND :end")
    List<Ticket> findCompletedByDeveloperInPeriod(
            @Param("developerId") Long developerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT t FROM Ticket t WHERE t.sprint.id = :sprintId AND t.completedAt IS NOT NULL")
    List<Ticket> findCompletedBySprintId(@Param("sprintId") Long sprintId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.sprint.id = :sprintId AND t.status IN :statuses")
    long countBySprintIdAndStatusIn(@Param("sprintId") Long sprintId, @Param("statuses") List<TicketStatus> statuses);

    @Query("SELECT COALESCE(SUM(t.storyPoints), 0) FROM Ticket t WHERE t.sprint.id = :sprintId AND t.status IN :statuses")
    int sumStoryPointsBySprintIdAndStatusIn(@Param("sprintId") Long sprintId, @Param("statuses") List<TicketStatus> statuses);

    @Query("SELECT t FROM Ticket t WHERE t.status = 'BLOCKED'")
    List<Ticket> findBlockedTickets();

    @Query("SELECT t FROM Ticket t WHERE t.status = 'IN_PROGRESS' AND t.startedAt < :threshold ORDER BY t.startedAt ASC")
    List<Ticket> findStaleTickets(@Param("threshold") LocalDateTime threshold);

    boolean existsByExternalIdAndSource(String externalId, TicketSource source);
}
