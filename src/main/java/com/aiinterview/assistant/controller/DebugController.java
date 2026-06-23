package com.aiinterview.assistant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight debug endpoint — hit GET /api/debug/token with a Bearer token
 * to verify the token is being received and validated correctly.
 * Remove this controller before deploying to production.
 */
@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/token")
    public ResponseEntity<Map<String, Object>> checkToken(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of(
                "authenticated", false,
                "message", "No valid token received by the server"
            ));
        }
        return ResponseEntity.ok(Map.of(
            "authenticated", true,
            "username", authentication.getName(),
            "authorities", authentication.getAuthorities().toString()
        ));
    }

    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> publicCheck() {
        return ResponseEntity.ok(Map.of("status", "Backend is reachable"));
    }
}
