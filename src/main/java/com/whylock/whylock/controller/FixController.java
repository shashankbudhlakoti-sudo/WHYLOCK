package com.whylock.whylock.controller;

import com.whylock.whylock.service.AiFixAssistantService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fix")
public class FixController {

    private final AiFixAssistantService aiFixAssistantService;

    public FixController(AiFixAssistantService aiFixAssistantService) {
        this.aiFixAssistantService = aiFixAssistantService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        String context = body.get("context");

        if (message == null || message.isBlank()) {
            return "Please type a question first.";
        }

        return aiFixAssistantService.chat(message, context);
    }
}