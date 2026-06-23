package com.aiinterview.assistant.repository;

import com.aiinterview.assistant.entity.Interview;
import com.aiinterview.assistant.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByUserOrderByCreatedAtDesc(User user);

    Optional<Interview> findByIdAndUser(Long id, User user);
}
