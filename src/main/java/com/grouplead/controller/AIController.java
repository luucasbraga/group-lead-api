package com.grouplead.controller;

import com.grouplead.domain.enums.PeriodType;
import com.grouplead.domain.entity.User;
import com.grouplead.dto.request.ChatRequest;
import com.grouplead.dto.response.*;
import com.grouplead.repository.UserRepository;
import com.grouplead.service.ai.AIOrchestrationService;
import com.grouplead.service.ai.PredictionService;
import com.grouplead.service.ai.SprintSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI-powered insights and chat endpoints")
public class AIController {

    private final AIOrchestrationService aiService;
    private final PredictionService predictionService;
    private final SprintSummaryService summaryService;
    private final UserRepository userRepository;

    @PostMapping("/chat")
    @Operation(summary = "Chat with AI assistant")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(aiService.chat(user.getId(), request));
    }

    @GetMapping("/predictions/sprint/{sprintId}")
    @Operation(summary = "Get sprint delivery prediction")
    public ResponseEntity<PredictionResponse> getSprintPrediction(@PathVariable String sprintId) {
        var prediction = predictionService.predictSprintCompletion(sprintId);
        return ResponseEntity.ok(PredictionResponse.from(prediction));
    }

    @GetMapping("/summary/sprint/{sprintId}")
    @Operation(summary = "Get AI-generated sprint summary")
    public ResponseEntity<SprintSummaryResponse> getSprintSummary(
            @PathVariable String sprintId,
            @RequestParam(defaultValue = "false") boolean regenerate) {

        return ResponseEntity.ok(summaryService.getSummary(sprintId, regenerate));
    }

    @GetMapping("/insights/developer/{developerId}")
    @Operation(summary = "Get AI insights for developer 1:1")
    public ResponseEntity<DeveloperInsightsResponse> getDeveloperInsights(
            @PathVariable Long developerId,
            @RequestParam(defaultValue = "MONTHLY") PeriodType period) {

        return ResponseEntity.ok(aiService.generateDeveloperInsights(developerId, period));
    }

    @GetMapping("/anomalies")
    @Operation(summary = "Get detected anomalies")
    public ResponseEntity<List<AnomalyResponse>> getAnomalies(
            @RequestParam(defaultValue = "24") int hoursBack) {

        return ResponseEntity.ok(aiService.getRecentAnomalies(hoursBack));
    }

    @GetMapping("/burnout-risk")
    @Operation(summary = "Get burnout risk analysis")
    public ResponseEntity<List<BurnoutRiskResponse>> getBurnoutRisk() {
        return ResponseEntity.ok(aiService.analyzeBurnoutRisk());
    }
}
