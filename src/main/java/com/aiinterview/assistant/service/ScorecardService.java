package com.aiinterview.assistant.service;

import com.aiinterview.assistant.dto.ScorecardDtos.AiEvaluationResult;
import com.aiinterview.assistant.dto.ScorecardDtos.QuestionAnswerPair;
import com.aiinterview.assistant.dto.ScorecardDtos.QuestionFeedback;
import com.aiinterview.assistant.dto.ScorecardDtos.ScorecardResponse;
import com.aiinterview.assistant.entity.Interview;
import com.aiinterview.assistant.entity.InterviewStatus;
import com.aiinterview.assistant.entity.Scorecard;
import com.aiinterview.assistant.entity.User;
import com.aiinterview.assistant.exception.ResourceNotFoundException;
import com.aiinterview.assistant.repository.InterviewRepository;
import com.aiinterview.assistant.repository.ScorecardRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScorecardService {

    private final InterviewService interviewService;
    private final InterviewRepository interviewRepository;
    private final ScorecardRepository scorecardRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    /**
     * Generates (or regenerates) the AI scorecard for a completed interview:
     * sends every question/answer pair to the AI evaluator, stores the
     * structured result, and marks the interview as COMPLETED.
     */
    @Transactional
    public ScorecardResponse generateScorecard(User user, Long interviewId) {
        Interview interview = interviewService.findOwnedInterview(user, interviewId);

        if (interview.getQuestions().isEmpty()) {
            throw new IllegalStateException("Interview has no questions to evaluate");
        }

        List<QuestionAnswerPair> pairs = interview.getQuestions().stream()
                .map(q -> QuestionAnswerPair.builder()
                        .questionId(q.getId())
                        .questionText(q.getQuestionText())
                        .answerText(q.getAnswer() != null ? q.getAnswer().getAnswerText() : null)
                        .build())
                .toList();

        AiEvaluationResult result = aiService.evaluateAnswers(interview.getRole(), pairs);

        Scorecard scorecard = interview.getScorecard();
        if (scorecard == null) {
            scorecard = Scorecard.builder().interview(interview).build();
        }

        scorecard.setOverallScore(result.getOverallScore());
        scorecard.setStrengths(result.getStrengths());
        scorecard.setWeaknesses(result.getWeaknesses());
        scorecard.setFeedback(serializeFeedback(result.getFeedback()));

        interview.setScorecard(scorecard);
        interview.setStatus(InterviewStatus.COMPLETED);
        interviewRepository.save(interview);

        return toScorecardResponse(interview.getId(), scorecard);
    }

    @Transactional(readOnly = true)
    public ScorecardResponse getScorecard(User user, Long interviewId) {
        Interview interview = interviewService.findOwnedInterview(user, interviewId);

        Scorecard scorecard = scorecardRepository.findByInterviewId(interview.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No scorecard has been generated yet for interview " + interviewId));

        return toScorecardResponse(interview.getId(), scorecard);
    }

    private String serializeFeedback(List<QuestionFeedback> feedback) {
        try {
            return objectMapper.writeValueAsString(feedback);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize AI feedback", e);
        }
    }

    private List<QuestionFeedback> deserializeFeedback(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, QuestionFeedback.class));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize stored AI feedback", e);
        }
    }

    private ScorecardResponse toScorecardResponse(Long interviewId, Scorecard scorecard) {
        return ScorecardResponse.builder()
                .interviewId(interviewId)
                .overallScore(scorecard.getOverallScore())
                .strengths(scorecard.getStrengths())
                .weaknesses(scorecard.getWeaknesses())
                .feedback(deserializeFeedback(scorecard.getFeedback()))
                .generatedAt(scorecard.getGeneratedAt())
                .build();
    }
}
