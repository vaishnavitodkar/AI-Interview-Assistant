package com.aiinterview.assistant.service;

import com.aiinterview.assistant.dto.InterviewDtos.AnswerSubmitRequest;
import com.aiinterview.assistant.dto.InterviewDtos.CreateInterviewRequest;
import com.aiinterview.assistant.dto.InterviewDtos.InterviewResponse;
import com.aiinterview.assistant.dto.InterviewDtos.InterviewSummary;
import com.aiinterview.assistant.dto.InterviewDtos.QuestionDto;
import com.aiinterview.assistant.entity.*;
import com.aiinterview.assistant.exception.ResourceNotFoundException;
import com.aiinterview.assistant.repository.AnswerRepository;
import com.aiinterview.assistant.repository.InterviewRepository;
import com.aiinterview.assistant.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final AiService aiService;

    /**
     * Creates a new interview: calls the AI service to generate questions for
     * the requested role/difficulty, then persists the interview and its
     * generated questions.
     */
    @Transactional
    public InterviewResponse createInterview(User user, CreateInterviewRequest request) {
        List<String> generatedQuestions = aiService.generateQuestions(
                request.getRole(), request.getDifficulty(), request.getNumberOfQuestions());

        Interview interview = Interview.builder()
                .user(user)
                .role(request.getRole())
                .difficulty(request.getDifficulty())
                .status(InterviewStatus.CREATED)
                .build();

        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < generatedQuestions.size(); i++) {
            questions.add(Question.builder()
                    .interview(interview)
                    .questionText(generatedQuestions.get(i))
                    .orderIndex(i + 1)
                    .build());
        }
        interview.setQuestions(questions);

        Interview saved = interviewRepository.save(interview);
        return toInterviewResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InterviewSummary> getInterviewHistory(User user) {
        return interviewRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toInterviewSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public InterviewResponse getInterviewById(User user, Long interviewId) {
        Interview interview = findOwnedInterview(user, interviewId);
        return toInterviewResponse(interview);
    }

    /**
     * Saves (or updates) the candidate's answer to a specific question and
     * marks the interview as IN_PROGRESS.
     */
    @Transactional
    public InterviewResponse submitAnswer(User user, Long interviewId, Long questionId, AnswerSubmitRequest request) {
        Interview interview = findOwnedInterview(user, interviewId);

        Question question = interview.getQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question " + questionId + " not found in interview " + interviewId));

        Answer answer = answerRepository.findByQuestionId(questionId)
                .orElse(Answer.builder().question(question).build());

        answer.setAnswerText(request.getAnswerText());
        answerRepository.save(answer);

        if (interview.getStatus() == InterviewStatus.CREATED) {
            interview.setStatus(InterviewStatus.IN_PROGRESS);
            interviewRepository.save(interview);
        }

        // Refresh to include the newly saved answer
        Interview refreshed = findOwnedInterview(user, interviewId);
        return toInterviewResponse(refreshed);
    }

    /**
     * Fetches an interview and verifies it belongs to the requesting user,
     * preventing one user from accessing another's interview data.
     */
    Interview findOwnedInterview(User user, Long interviewId) {
        return interviewRepository.findByIdAndUser(interviewId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Interview " + interviewId + " not found for current user"));
    }

    private InterviewResponse toInterviewResponse(Interview interview) {
        List<QuestionDto> questionDtos = interview.getQuestions().stream()
                .map(q -> QuestionDto.builder()
                        .id(q.getId())
                        .orderIndex(q.getOrderIndex())
                        .questionText(q.getQuestionText())
                        .answerText(q.getAnswer() != null ? q.getAnswer().getAnswerText() : null)
                        .build())
                .toList();

        return InterviewResponse.builder()
                .id(interview.getId())
                .role(interview.getRole())
                .difficulty(interview.getDifficulty())
                .status(interview.getStatus())
                .createdAt(interview.getCreatedAt())
                .questions(questionDtos)
                .build();
    }

    private InterviewSummary toInterviewSummary(Interview interview) {
        Double score = interview.getScorecard() != null ? interview.getScorecard().getOverallScore() : null;

        return InterviewSummary.builder()
                .id(interview.getId())
                .role(interview.getRole())
                .difficulty(interview.getDifficulty())
                .status(interview.getStatus())
                .createdAt(interview.getCreatedAt())
                .overallScore(score)
                .build();
    }
}
