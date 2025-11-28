package com.grouplead.repository;

import com.grouplead.domain.entity.AIInsight;
import com.grouplead.domain.enums.InsightType;
import com.grouplead.domain.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AIInsightRepository extends JpaRepository<AIInsight, Long> {

    List<AIInsight> findByType(InsightType type);

    List<AIInsight> findByTargetType(TargetType targetType);

    @Query("SELECT ai FROM AIInsight ai WHERE ai.type = :type AND ai.targetId = :targetId ORDER BY ai.generatedAt DESC LIMIT 1")
    Optional<AIInsight> findLatestByTypeAndTargetId(@Param("type") InsightType type, @Param("targetId") String targetId);

    @Query("SELECT ai FROM AIInsight ai WHERE ai.targetId = :targetId AND ai.targetType = :targetType ORDER BY ai.generatedAt DESC")
    List<AIInsight> findByTarget(@Param("targetId") String targetId, @Param("targetType") TargetType targetType);

    @Query("SELECT ai FROM AIInsight ai WHERE ai.generatedAt >= :since ORDER BY ai.generatedAt DESC")
    List<AIInsight> findRecentInsights(@Param("since") LocalDateTime since);

    @Query("SELECT ai FROM AIInsight ai WHERE ai.expiresAt IS NULL OR ai.expiresAt > :now")
    List<AIInsight> findValidInsights(@Param("now") LocalDateTime now);

    @Query("SELECT ai FROM AIInsight ai WHERE ai.type = :type AND (ai.expiresAt IS NULL OR ai.expiresAt > :now) ORDER BY ai.generatedAt DESC")
    List<AIInsight> findValidByType(@Param("type") InsightType type, @Param("now") LocalDateTime now);

    @Query("DELETE FROM AIInsight ai WHERE ai.expiresAt < :now")
    void deleteExpired(@Param("now") LocalDateTime now);
}
