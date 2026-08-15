package com.aiinterview.assistant.service;

import com.aiinterview.assistant.dto.ScorecardDtos.AiEvaluationResult;
import com.aiinterview.assistant.dto.ScorecardDtos.QuestionAnswerPair;
import com.aiinterview.assistant.dto.ScorecardDtos.QuestionFeedback;
import com.aiinterview.assistant.entity.Difficulty;
import com.aiinterview.assistant.exception.AiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAiService implements AiService {

    private final HttpClient aiHttpClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.gemini.api-key}")
    private String apiKey;

    @Value("${ai.gemini.model}")
    private String model;

    @Value("${ai.gemini.base-url}")
    private String baseUrl;

    @Value("${ai.gemini.max-tokens}")
    private int maxTokens;

    @Override
    public List<String> generateQuestions(String role, Difficulty difficulty, int count) {
        String systemPrompt = """
                You are an expert technical interviewer. Generate realistic, role-specific
                interview questions. Respond with ONLY a valid JSON array of strings -
                no markdown formatting, no code fences, no explanation, no extra text.
                Each string is one interview question.
                """;

        String userPrompt = String.format("""
                Generate exactly %d interview questions for a candidate applying for the
                role of "%s" at %s difficulty level.

                Guidelines:
                - Mix conceptual questions, practical/scenario-based questions, and at least
                  one problem-solving or system-design style question if the role and
                  difficulty allow it.
                - Questions should be specific enough to be answerable in a few sentences.
                - Do not number the questions; return them as plain strings in a JSON array.
                """, count, role, difficulty.name());

        String rawResponse = callGemini(systemPrompt, userPrompt);
        String json = extractJson(rawResponse);

        try {
            ArrayNode array = (ArrayNode) objectMapper.readTree(json);
            List<String> questions = new ArrayList<>();
            for (JsonNode node : array) {
                questions.add(node.asText());
            }
            if (questions.isEmpty()) {
                throw new AiServiceException("AI returned an empty question list");
            }
            return questions;
        } catch (Exception e) {
            log.error("Failed to parse AI question generation response: {}", rawResponse, e);
            throw new AiServiceException("Failed to parse AI-generated questions", e);
        }
    }

    @Override
    public AiEvaluationResult evaluateAnswers(String role, List<QuestionAnswerPair> pairs) {
        String systemPrompt = """
                You are an expert technical interviewer evaluating a candidate's mock
                interview responses. Respond with ONLY valid JSON matching exactly this
                schema - no markdown formatting, no code fences, no explanation:

                {
                  "overallScore": number between 0 and 100,
                  "strengths": "short paragraph summarizing the candidate's strengths",
                  "weaknesses": "short paragraph summarizing areas for improvement",
                  "feedback": [
                    {
                      "questionId": number,
                      "comment": "1-3 sentence feedback on this specific answer",
                      "score": number between 0 and 10
                    }
                  ]
                }

                Evaluate based on technical correctness, clarity, depth, and relevance to
                the question. If an answer is empty or "I don't know", score it low but
                still provide constructive feedback.
                """;

        StringBuilder qaBuilder = new StringBuilder();
        for (QuestionAnswerPair pair : pairs) {
            qaBuilder.append(String.format(
                    "Question ID: %d%nQuestion: %s%nCandidate's Answer: %s%n%n",
                    pair.getQuestionId(),
                    pair.getQuestionText(),
                    (pair.getAnswerText() == null || pair.getAnswerText().isBlank())
                            ? "(no answer provided)"
                            : pair.getAnswerText()
            ));
        }

        String userPrompt = String.format("""
                The candidate is interviewing for the role of "%s".
                Below are the interview questions and the candidate's answers.
                Evaluate the full interview and respond with the JSON schema described.

                %s
                """, role, qaBuilder);

        String rawResponse = callGemini(systemPrompt, userPrompt);
        String json = extractJson(rawResponse);

        try {
            ObjectNode root = (ObjectNode) objectMapper.readTree(json);

            List<QuestionFeedback> feedbackList = new ArrayList<>();
            ArrayNode feedbackArray = (ArrayNode) root.get("feedback");
            if (feedbackArray != null) {
                for (JsonNode node : feedbackArray) {
                    long questionId = node.get("questionId").asLong();

                    // Attach the original question text so the frontend doesn't
                    // need a second lookup.
                    String questionText = pairs.stream()
                            .filter(p -> p.getQuestionId() == questionId)
                            .map(QuestionAnswerPair::getQuestionText)
                            .findFirst()
                            .orElse("");

                    feedbackList.add(QuestionFeedback.builder()
                            .questionId(questionId)
                            .questionText(questionText)
                            .comment(node.get("comment").asText())
                            .score(node.get("score").asDouble())
                            .build());
                }
            }

            return AiEvaluationResult.builder()
                    .overallScore(root.get("overallScore").asDouble())
                    .strengths(root.get("strengths").asText())
                    .weaknesses(root.get("weaknesses").asText())
                    .feedback(feedbackList)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse AI evaluation response: {}", rawResponse, e);
            throw new AiServiceException("Failed to parse AI evaluation result", e);
        }
    }

 
    private String callGemini(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiServiceException(
                    "GEMINI_API_KEY is not configured. Set it as an environment variable.");
        }

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();

            ObjectNode systemInstruction = requestBody.putObject("systemInstruction");
            ArrayNode systemParts = systemInstruction.putArray("parts");
            systemParts.addObject().put("text", systemPrompt);

            ArrayNode contents = requestBody.putArray("contents");
            ObjectNode userContent = contents.addObject();
            userContent.put("role", "user");
            ArrayNode userParts = userContent.putArray("parts");
            userParts.addObject().put("text", userPrompt);

            ObjectNode generationConfig = requestBody.putObject("generationConfig");
            generationConfig.put("maxOutputTokens", maxTokens);
            generationConfig.put("temperature", 0.7);

        
            String url = baseUrl + "/" + model + ":generateContent";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = aiHttpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini API error ({}): {}", response.statusCode(), response.body());
                throw new AiServiceException(
                        "Gemini API returned status " + response.statusCode() + ": " + response.body());
            }

            JsonNode responseJson = objectMapper.readTree(response.body());
            JsonNode candidates = responseJson.get("candidates");

            StringBuilder textBuilder = new StringBuilder();
            if (candidates != null && candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray()) {
                    for (JsonNode part : parts) {
                        textBuilder.append(part.path("text").asText());
                    }
                }
            }

            String text = textBuilder.toString();
            if (text.isBlank()) {
                
                JsonNode promptFeedback = responseJson.get("promptFeedback");
                if (promptFeedback != null) {
                    throw new AiServiceException(
                            "Gemini API blocked or returned no content: " + promptFeedback);
                }
                throw new AiServiceException("Gemini API returned an empty response");
            }
            return text;

        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            throw new AiServiceException("Error calling Gemini API", e);
        }
    }

   
    private String extractJson(String text) {
        String trimmed = text.trim();

        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline != -1 && lastFence > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }

        return trimmed;
    }
}
