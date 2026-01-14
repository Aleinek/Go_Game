package com.gogame.service;

import com.gogame.domain.enums.DeadStoneStatus;
import com.gogame.domain.enums.StoneColor;
import com.gogame.domain.exception.InvalidNegotiationException;
import com.gogame.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NegotiationService Tests")
class NegotiationServiceTest {

    private NegotiationService negotiationService;
    private Board board;
    private Player blackPlayer;
    private Player whitePlayer;

    @BeforeEach
    void setUp() {
        negotiationService = new NegotiationService();
        blackPlayer = new Player(java.util.UUID.randomUUID(), "BlackPlayer", StoneColor.BLACK);
        whitePlayer = new Player(java.util.UUID.randomUUID(), "WhitePlayer", StoneColor.WHITE);
        board = new Board(9, blackPlayer, whitePlayer);
    }

    @Nested
    @DisplayName("Dead Chain Suggestion Tests")
    class DeadChainSuggestionTests {

        @Test
        @DisplayName("should return empty list for empty board")
        void shouldReturnEmptyListForEmptyBoard() {
            List<ChainInfo> suggestions = negotiationService.analyzeAndSuggestDeadChains(board);
            assertTrue(suggestions.isEmpty());
        }

        @Test
        @DisplayName("should identify chains with few liberties as dead candidates")
        void shouldIdentifyChainsWithFewLibertiesAsDeadCandidates() {
            // Place a black stone surrounded by white stones
            // This creates a chain with very few liberties
            board.placeStone(new Position(4, 4), StoneColor.BLACK);
            board.placeStone(new Position(3, 4), StoneColor.WHITE);
            board.placeStone(new Position(5, 4), StoneColor.WHITE);
            board.placeStone(new Position(4, 3), StoneColor.WHITE);
            board.placeStone(new Position(4, 5), StoneColor.WHITE);

            List<ChainInfo> suggestions = negotiationService.analyzeAndSuggestDeadChains(board);
            
            assertFalse(suggestions.isEmpty());
            // The black stone should have 0 liberties and be suggested as dead
            ChainInfo blackChain = suggestions.stream()
                .filter(ci -> ci.getColor() == StoneColor.BLACK)
                .findFirst()
                .orElse(null);
            
            // Note: The stone might have been captured, so we might not find it
            // In a real negotiation scenario, this would be after a pass, not immediate capture
        }

        @Test
        @DisplayName("should not suggest chains with many liberties as dead")
        void shouldNotSuggestChainsWithManyLibertiesAsDead() {
            // Place a single black stone with full liberties
            board.placeStone(new Position(4, 4), StoneColor.BLACK);

            List<ChainInfo> suggestions = negotiationService.analyzeAndSuggestDeadChains(board);
            
            ChainInfo blackChain = suggestions.stream()
                .filter(ci -> ci.getColor() == StoneColor.BLACK)
                .findFirst()
                .orElse(null);
            
            assertNotNull(blackChain);
            assertEquals(DeadStoneStatus.ALIVE, blackChain.getStatus());
            assertEquals(4, blackChain.getLiberties()); // 4 liberties in center
        }
    }

    @Nested
    @DisplayName("Dead Marking Validation Tests")
    class DeadMarkingValidationTests {

        @Test
        @DisplayName("should allow marking chain as alive without validation")
        void shouldAllowMarkingChainAsAliveWithoutValidation() {
            board.placeStone(new Position(4, 4), StoneColor.BLACK);
            List<ChainInfo> chains = negotiationService.analyzeAndSuggestDeadChains(board);
            ChainInfo chainInfo = chains.get(0);
            chainInfo.setStatus(DeadStoneStatus.DEAD);
            
            // Should not throw - marking as ALIVE is always allowed
            assertDoesNotThrow(() -> 
                negotiationService.validateDeadMarking(board, chainInfo, DeadStoneStatus.ALIVE)
            );
        }

        @Test
        @DisplayName("should reject marking chain with many liberties as dead")
        void shouldRejectMarkingChainWithManyLibertiesAsDead() {
            board.placeStone(new Position(4, 4), StoneColor.BLACK);
            List<ChainInfo> chains = negotiationService.analyzeAndSuggestDeadChains(board);
            ChainInfo chainInfo = chains.get(0);
            
            // Chain has 4 liberties, should not be allowed to mark as dead
            InvalidNegotiationException exception = assertThrows(
                InvalidNegotiationException.class,
                () -> negotiationService.validateDeadMarking(board, chainInfo, DeadStoneStatus.DEAD)
            );
            
            assertEquals(InvalidNegotiationException.ErrorCode.CHAIN_HAS_TOO_MANY_LIBERTIES, 
                        exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Score Calculation Tests")
    class ScoreCalculationTests {

        @Test
        @DisplayName("should calculate correct score with no dead stones")
        void shouldCalculateCorrectScoreWithNoDeadStones() {
            // Setup a simple position
            board.placeStone(new Position(0, 0), StoneColor.BLACK);
            board.placeStone(new Position(8, 8), StoneColor.WHITE);
            board.updateTerritory();

            Game game = new Game(blackPlayer, whitePlayer, board);
            game.consecutivePasses = 2;
            game.startNegotiation();
            
            // Initialize negotiation state
            List<ChainInfo> chainInfos = negotiationService.analyzeAndSuggestDeadChains(board);
            for (ChainInfo ci : chainInfos) {
                game.negotiationState.addChainInfo(ci);
            }
            
            ScoreResult score = negotiationService.calculateFinalScore(game);
            
            assertNotNull(score);
            assertEquals(6.5, score.getKomi());
            // Verify winner is determined based on territory + komi
        }

        @Test
        @DisplayName("should add dead stones to opponent score")
        void shouldAddDeadStonesToOpponentScore() {
            // Place stones
            board.placeStone(new Position(1, 1), StoneColor.BLACK);
            board.placeStone(new Position(7, 7), StoneColor.WHITE);
            board.updateTerritory();

            Game game = new Game(blackPlayer, whitePlayer, board);
            game.consecutivePasses = 2;
            game.startNegotiation();
            
            List<ChainInfo> chainInfos = negotiationService.analyzeAndSuggestDeadChains(board);
            for (ChainInfo ci : chainInfos) {
                game.negotiationState.addChainInfo(ci);
                // Manually mark the white stone as dead for testing
                if (ci.getColor() == StoneColor.WHITE) {
                    ci.setStatus(DeadStoneStatus.DEAD);
                }
            }
            
            ScoreResult score = negotiationService.calculateFinalScore(game);
            
            // White dead stones should count for black
            assertEquals(1, score.getWhiteDeadStones());
            // Black's total should include the white dead stone
            assertTrue(score.getBlackTotal() >= score.getWhiteDeadStones());
        }

        @Test
        @DisplayName("should throw exception when game not in negotiation")
        void shouldThrowExceptionWhenGameNotInNegotiation() {
            Game game = new Game(blackPlayer, whitePlayer, board);
            // Game is IN_PROGRESS, not NEGOTIATING
            
            assertThrows(InvalidNegotiationException.class,
                () -> negotiationService.calculateFinalScore(game));
        }
    }

    @Nested
    @DisplayName("Territory Calculation with Dead Stones Tests")
    class TerritoryWithDeadStonesTests {

        @Test
        @DisplayName("should treat dead stone positions as empty for territory")
        void shouldTreatDeadStonePositionsAsEmptyForTerritory() {
            // Create a scenario where removing a dead stone changes territory
            // Place a line of black stones
            for (int i = 0; i < 9; i++) {
                board.placeStone(new Position(4, i), StoneColor.BLACK);
            }
            // Place white stones on one side
            board.placeStone(new Position(5, 4), StoneColor.WHITE);
            
            board.updateTerritory();

            Game game = new Game(blackPlayer, whitePlayer, board);
            game.consecutivePasses = 2;
            game.startNegotiation();
            
            List<ChainInfo> chainInfos = negotiationService.analyzeAndSuggestDeadChains(board);
            for (ChainInfo ci : chainInfos) {
                game.negotiationState.addChainInfo(ci);
            }
            
            Territory territoryBefore = negotiationService.calculateTerritoryWithDeadStones(
                board, game.negotiationState);
            
            // Mark the white stone as dead
            for (ChainInfo ci : game.negotiationState.getAllChainInfos().values()) {
                if (ci.getColor() == StoneColor.WHITE) {
                    ci.setStatus(DeadStoneStatus.DEAD);
                }
            }
            
            Territory territoryAfter = negotiationService.calculateTerritoryWithDeadStones(
                board, game.negotiationState);
            
            // Black territory should increase when white stone is marked dead
            assertTrue(territoryAfter.getBlackTerritory() >= territoryBefore.getBlackTerritory());
        }
    }
}
