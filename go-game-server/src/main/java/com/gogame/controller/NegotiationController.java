package com.gogame.controller;

import com.gogame.domain.model.ChainInfo;
import com.gogame.domain.model.NegotiationState;
import com.gogame.domain.model.ScoreResult;
import com.gogame.dto.request.ToggleChainStatusRequest;
import com.gogame.dto.response.NegotiationStateResponse;
import com.gogame.dto.response.ScoreResponse;
import com.gogame.dto.response.ToggleChainResponse;
import com.gogame.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for scoring negotiation phase endpoints.
 */
@RestController
@RequestMapping("/api/games/{gameId}/negotiation")
public class NegotiationController {

    private final GameService gameService;

    public NegotiationController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Get current negotiation state including chains and acceptance status.
     */
    @GetMapping
    public ResponseEntity<NegotiationStateResponse> getNegotiationState(
            @PathVariable UUID gameId,
            @RequestHeader("X-Player-Id") UUID playerId) {
        
        NegotiationState state = gameService.getNegotiationState(gameId, playerId);
        ScoreResult score = gameService.getScorePreview(gameId, playerId);
        
        NegotiationStateResponse response = buildNegotiationStateResponse(
            gameId, state, score, "Negotiation state retrieved"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Toggle a chain's dead/alive status.
     * POST /api/games/{gameId}/negotiation/toggle
     */
    @PostMapping("/toggle")
    public ResponseEntity<ToggleChainResponse> toggleChainStatus(
            @PathVariable UUID gameId,
            @RequestHeader("X-Player-Id") UUID playerId,
            @RequestBody ToggleChainStatusRequest request) {
        
        gameService.toggleChainStatus(gameId, playerId, request.chainId());
        
        NegotiationState state = gameService.getNegotiationState(gameId, playerId);
        ChainInfo chainInfo = state.getChainInfo(request.chainId());
        
        ToggleChainResponse response = new ToggleChainResponse(
            request.chainId(),
            chainInfo.getStatus().toString(),
            state.isBlackAccepted(),
            state.isWhiteAccepted(),
            "Chain status toggled successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Accept the current score.
     * POST /api/games/{gameId}/negotiation/accept
     */
    @PostMapping("/accept")
    public ResponseEntity<Map<String, Object>> acceptScore(
            @PathVariable UUID gameId,
            @RequestHeader("X-Player-Id") UUID playerId) {
        
        gameService.acceptScore(gameId, playerId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Score accepted"
        ));
    }

    /**
     * Resume playing from negotiation phase.
     * POST /api/games/{gameId}/negotiation/resume
     */
    @PostMapping("/resume")
    public ResponseEntity<Map<String, Object>> resumePlay(
            @PathVariable UUID gameId,
            @RequestHeader("X-Player-Id") UUID playerId) {
        
        gameService.resumePlay(gameId, playerId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Game resumed. Negotiation cancelled."
        ));
    }

    /**
     * Get current score preview (without ending the game).
     * GET /api/games/{gameId}/negotiation/score
     */
    @GetMapping("/score")
    public ResponseEntity<ScoreResponse> getScorePreview(
            @PathVariable UUID gameId,
            @RequestHeader("X-Player-Id") UUID playerId) {
        
        ScoreResult score = gameService.getScorePreview(gameId, playerId);
        
        ScoreResponse response = new ScoreResponse(
            score.getBlackTerritory(),
            score.getWhiteTerritory(),
            score.getBlackPrisoners(),
            score.getWhitePrisoners(),
            score.getBlackDeadStones(),
            score.getWhiteDeadStones(),
            score.getKomi(),
            score.getBlackTotal(),
            score.getWhiteTotal(),
            score.getWinner(),
            score.getScoreDifference(),
            "Score preview calculated"
        );
        
        return ResponseEntity.ok(response);
    }

    private NegotiationStateResponse buildNegotiationStateResponse(
            UUID gameId, NegotiationState state, ScoreResult score, String message) {
        
        List<NegotiationStateResponse.ChainInfoDto> chains = state.getAllChainInfos().values()
            .stream()
            .map(ci -> new NegotiationStateResponse.ChainInfoDto(
                ci.getChainId(),
                ci.getPositions().stream()
                    .map(p -> new NegotiationStateResponse.PositionDto(p.getX(), p.getY()))
                    .collect(Collectors.toList()),
                ci.getColor().toString(),
                ci.getLiberties(),
                ci.getStatus().toString()
            ))
            .collect(Collectors.toList());
        
        NegotiationStateResponse.ScorePreviewDto scorePreview = 
            new NegotiationStateResponse.ScorePreviewDto(
                score.getBlackTerritory(),
                score.getWhiteTerritory(),
                score.getBlackPrisoners(),
                score.getWhitePrisoners(),
                score.getBlackDeadStones(),
                score.getWhiteDeadStones(),
                score.getKomi(),
                score.getBlackTotal(),
                score.getWhiteTotal(),
                score.getWinner(),
                score.getScoreDifference()
            );
        
        return new NegotiationStateResponse(
            gameId,
            chains,
            state.isBlackAccepted(),
            state.isWhiteAccepted(),
            6.5, // Default komi - could be retrieved from game
            scorePreview,
            message
        );
    }
}
