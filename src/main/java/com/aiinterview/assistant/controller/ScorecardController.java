package com.aiinterview.assistant.controller;

import com.aiinterview.assistant.dto.ScorecardDtos.ScorecardResponse;
import com.aiinterview.assistant.entity.User;
import com.aiinterview.assistant.security.CurrentUserProvider;
import com.aiinterview.assistant.service.ScorecardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interviews/{id}/scorecard")
@RequiredArgsConstructor
public class ScorecardController {

    private final ScorecardService scorecardService;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Triggers AI evaluation of all answers submitted for this interview and
     * persists the resulting scorecard. Marks the interview as COMPLETED.
     */
    @PostMapping
    public ResponseEntity<ScorecardResponse> generateScorecard(
            @PathVariable Long id,
            Authentication authentication) {

        User user = currentUserProvider.getCurrentUser(authentication);
        return ResponseEntity.ok(scorecardService.generateScorecard(user, id));
    }

    /**
     * Retrieves a previously generated scorecard.
     */
    @GetMapping
    public ResponseEntity<ScorecardResponse> getScorecard(
            @PathVariable Long id,
            Authentication authentication) {

        User user = currentUserProvider.getCurrentUser(authentication);
        return ResponseEntity.ok(scorecardService.getScorecard(user, id));
    }
}
