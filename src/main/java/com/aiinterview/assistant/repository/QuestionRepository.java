package com.aiinterview.assistant.repository;

import com.aiinterview.assistant.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByInterviewIdOrderByOrderIndexAsc(Long interviewId);
}
