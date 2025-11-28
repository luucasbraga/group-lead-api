package com.grouplead.repository;

import com.grouplead.domain.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    List<ChatHistory> findByUserId(Long userId);

    List<ChatHistory> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    @Query("SELECT ch FROM ChatHistory ch WHERE ch.user.id = :userId AND ch.sessionId = :sessionId ORDER BY ch.createdAt ASC")
    List<ChatHistory> findByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") String sessionId);

    @Query("SELECT ch FROM ChatHistory ch WHERE ch.user.id = :userId ORDER BY ch.createdAt DESC LIMIT :limit")
    List<ChatHistory> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Query("SELECT DISTINCT ch.sessionId FROM ChatHistory ch WHERE ch.user.id = :userId ORDER BY MAX(ch.createdAt) DESC")
    List<String> findRecentSessionsByUserId(@Param("userId") Long userId);

    @Query("DELETE FROM ChatHistory ch WHERE ch.createdAt < :before")
    void deleteOlderThan(@Param("before") LocalDateTime before);

    @Query("SELECT COUNT(ch) FROM ChatHistory ch WHERE ch.user.id = :userId AND ch.createdAt >= :since")
    long countMessagesByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
