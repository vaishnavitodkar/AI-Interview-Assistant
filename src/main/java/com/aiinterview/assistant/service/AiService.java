package com.aiinterview.assistant.service;

import com.aiinterview.assistant.dto.ScorecardDtos.AiEvaluationResult;
import com.aiinterview.assistant.dto.ScorecardDtos.QuestionAnswerPair;
import com.aiinterview.assistant.entity.Difficulty;

import java.util.List;

/**
 * Abstraction over the LLM provider used for question generation and answer
 * evaluation. Implementations (e.g. AnthropicAiService) can be swapped out
 * via Spring's dependency injection without touching business logic.
 */
public interface AiService {

    /**
     * Generates a list of interview questions for the given role and difficulty.
     *
     * @param role       target job role / topic, e.g. "Backend Developer - Java"
     * @param difficulty desired difficulty level
     * @param count      number of questions to generate
     * @return list of question texts
     */
    List<String> generateQuestions(String role, Difficulty difficulty, int count);

    /**
     * Evaluates a completed interview's question/answer pairs and produces
     * an overall score, qualitative feedback and per-question comments.
     *
     * @param role  the role the interview was targeting (used for context)
     * @param pairs question/answer pairs submitted by the candidate
     * @return structured evaluation result
     */
    AiEvaluationResult evaluateAnswers(String role, List<QuestionAnswerPair> pairs);
}
