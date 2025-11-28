package com.grouplead.repository;

import com.grouplead.domain.entity.Developer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {

    Optional<Developer> findByEmail(String email);

    List<Developer> findByTeamId(Long teamId);

    List<Developer> findByActiveTrue();

    @Query("SELECT d FROM Developer d WHERE d.team.id = :teamId AND d.active = true")
    List<Developer> findActiveByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT d FROM Developer d JOIN d.externalIds e WHERE KEY(e) = :source AND VALUE(e) = :externalId")
    Optional<Developer> findByExternalId(@Param("source") String source, @Param("externalId") String externalId);

    @Query("SELECT d FROM Developer d LEFT JOIN FETCH d.tickets WHERE d.id = :id")
    Optional<Developer> findByIdWithTickets(@Param("id") Long id);

    @Query("SELECT d FROM Developer d LEFT JOIN FETCH d.commits WHERE d.id = :id")
    Optional<Developer> findByIdWithCommits(@Param("id") Long id);

    boolean existsByEmail(String email);
}
