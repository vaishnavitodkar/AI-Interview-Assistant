package com.aiinterview.assistant.controller;

import com.aiinterview.assistant.dto.InterviewDtos.AnswerSubmitRequest;
import com.aiinterview.assistant.dto.InterviewDtos.CreateInterviewRequest;
import com.aiinterview.assistant.dto.InterviewDtos.InterviewResponse;
import com.aiinterview.assistant.dto.InterviewDtos.InterviewSummary;
import com.aiinterview.assistant.entity.User;
import com.aiinterview.assistant.security.CurrentUserProvider;
import com.aiinterview.assistant.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Creates a new mock interview. The AI service generates questions for
     * the requested role/difficulty and they are stored immediately.
     */
    @PostMapping
    public ResponseEntity<InterviewResponse> createInterview(
            @Valid @RequestBody CreateInterviewRequest request,
            Authentication authentication) {

        User user = currentUserProvider.getCurrentUser(authentication);
        InterviewResponse response = interviewService.createInterview(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns the current user's interview history (summary view).
     */
    @GetMapping
    public ResponseEntity<List<InterviewSummary>> getHistory(Authentication authentication) {
        User user = currentUserProvider.getCurrentUser(authentication);
        return ResponseEntity.ok(interviewService.getInterviewHistory(user));
    }

    /**
     * Returns full detail for a single interview, including questions and
     * any answers submitted so far.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponse> getInterview(
            @PathVariable Long id,
            Authentication authentication) {

        User user = currentUserProvider.getCurrentUser(authentication);
        return ResponseEntity.ok(interviewService.getInterviewById(user, id));
    }

    /**
     * Submits (or updates) the candidate's answer to a specific question.
     */
    @PostMapping("/{id}/questions/{questionId}/answer")
    public ResponseEntity<InterviewResponse> submitAnswer(
            @PathVariable Long id,
            @PathVariable Long questionId,
            @Valid @RequestBody AnswerSubmitRequest request,
            Authentication authentication) {

        User user = currentUserProvider.getCurrentUser(authentication);
        InterviewResponse response = interviewService.submitAnswer(user, id, questionId, request);
        return ResponseEntity.ok(response);
    }
}
