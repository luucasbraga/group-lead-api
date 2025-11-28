package com.grouplead.repository;

import com.grouplead.domain.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByName(String name);

    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.developers WHERE t.id = :id")
    Optional<Team> findByIdWithDevelopers(Long id);

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.developers")
    List<Team> findAllWithDevelopers();
}
