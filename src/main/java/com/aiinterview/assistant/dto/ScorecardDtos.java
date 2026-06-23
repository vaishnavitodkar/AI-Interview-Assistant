package com.aiinterview.assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ScorecardDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScorecardResponse {
        private Long interviewId;
        private Double overallScore;
        private String strengths;
        private String weaknesses;
        private List<QuestionFeedback> feedback;
        private LocalDateTime generatedAt;
    }

    /**
     * Per-question feedback returned by the AI evaluator.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionFeedback {
        private Long questionId;
        private String questionText;
        private String comment;
        private Double score; // out of 10
    }

    /**
     * Internal structure used to pass question/answer pairs to the AI service
     * and to receive the raw AI evaluation result before persistence.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionAnswerPair {
        private Long questionId;
        private String questionText;
        private String answerText;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AiEvaluationResult {
        private Double overallScore;
        private String strengths;
        private String weaknesses;
        private List<QuestionFeedback> feedback;
    }
}
