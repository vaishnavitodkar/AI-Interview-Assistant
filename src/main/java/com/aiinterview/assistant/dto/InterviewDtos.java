package com.aiinterview.assistant.dto;

import com.aiinterview.assistant.entity.Difficulty;
import com.aiinterview.assistant.entity.InterviewStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class InterviewDtos {

    /**
     * Request body for creating a new mock interview.
     * The AI service uses 'role' and 'difficulty' to generate relevant questions.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateInterviewRequest {

        @NotBlank(message = "Role is required, e.g. 'Backend Developer - Java'")
        private String role;

        @NotNull(message = "Difficulty is required")
        private Difficulty difficulty;

        @Min(value = 1, message = "Must request at least 1 question")
        @Max(value = 15, message = "Cannot request more than 15 questions")
        private int numberOfQuestions = 5;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionDto {
        private Long id;
        private Integer orderIndex;
        private String questionText;
        private String answerText; // null if not yet answered
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InterviewResponse {
        private Long id;
        private String role;
        private Difficulty difficulty;
        private InterviewStatus status;
        private LocalDateTime createdAt;
        private List<QuestionDto> questions;
    }

    /**
     * Lightweight summary used for interview history listings.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InterviewSummary {
        private Long id;
        private String role;
        private Difficulty difficulty;
        private InterviewStatus status;
        private LocalDateTime createdAt;
        private Double overallScore; // null if no scorecard generated yet
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerSubmitRequest {

        @NotBlank(message = "Answer text cannot be empty")
        private String answerText;
    }
}
