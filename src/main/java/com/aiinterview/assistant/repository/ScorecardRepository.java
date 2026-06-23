package com.aiinterview.assistant.repository;

import com.aiinterview.assistant.entity.Scorecard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScorecardRepository extends JpaRepository<Scorecard, Long> {

    Optional<Scorecard> findByInterviewId(Long interviewId);
}
