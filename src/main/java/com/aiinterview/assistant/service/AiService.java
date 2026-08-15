package com.aiinterview.assistant.service;

import com.aiinterview.assistant.dto.ScorecardDtos.AiEvaluationResult;
import com.aiinterview.assistant.dto.ScorecardDtos.QuestionAnswerPair;
import com.aiinterview.assistant.entity.Difficulty;

import java.util.List;


public interface AiService {

    /**
     *
     * @param role       target job role / topic, e.g. "Backend Developer - Java"
     * @param difficulty desired difficulty level
     * @param count      number of questions to generate
     * @return list of question texts
     */
    List<String> generateQuestions(String role, Difficulty difficulty, int count);

    /**
     *
     * @param role  the role the interview was targeting (used for context)
     * @param pairs question/answer pairs submitted by the candidate
     * @return structured evaluation result
     */
    AiEvaluationResult evaluateAnswers(String role, List<QuestionAnswerPair> pairs);
}
