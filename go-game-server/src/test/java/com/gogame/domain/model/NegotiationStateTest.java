package com.gogame.domain.model;

import com.gogame.domain.enums.DeadStoneStatus;
import com.gogame.domain.enums.StoneColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NegotiationState Tests")
class NegotiationStateTest {

    private NegotiationState state;

    @BeforeEach
    void setUp() {
        state = new NegotiationState();
    }

    @Nested
    @DisplayName("Acceptance Tests")
    class AcceptanceTests {

        @Test
        @DisplayName("should start with both players not accepted")
        void shouldStartWithBothPlayersNotAccepted() {
            assertFalse(state.isBlackAccepted());
            assertFalse(state.isWhiteAccepted());
            assertFalse(state.bothAccepted());
        }

        @Test
        @DisplayName("should track black acceptance")
        void shouldTrackBlackAcceptance() {
            state.setBlackAccepted(true);
            assertTrue(state.isBlackAccepted());
            assertFalse(state.isWhiteAccepted());
            assertFalse(state.bothAccepted());
        }

        @Test
        @DisplayName("should track white acceptance")
        void shouldTrackWhiteAcceptance() {
            state.setWhiteAccepted(true);
            assertFalse(state.isBlackAccepted());
            assertTrue(state.isWhiteAccepted());
            assertFalse(state.bothAccepted());
        }

        @Test
        @DisplayName("should return true when both accepted")
        void shouldReturnTrueWhenBothAccepted() {
            state.setBlackAccepted(true);
            state.setWhiteAccepted(true);
            assertTrue(state.bothAccepted());
        }

        @Test
        @DisplayName("should reset acceptance")
        void shouldResetAcceptance() {
            state.setBlackAccepted(true);
            state.setWhiteAccepted(true);
            state.resetAcceptance();
            assertFalse(state.isBlackAccepted());
            assertFalse(state.isWhiteAccepted());
        }
    }

    @Nested
    @DisplayName("Chain Status Toggle Tests")
    class ChainStatusToggleTests {

        private ChainInfo createTestChainInfo(int chainId, StoneColor color) {
            Stone stone = new Stone(new Position(0, 0), color);
            Chain chain = new Chain(stone, color);
            return new ChainInfo(chainId, chain, 4);
        }

        @Test
        @DisplayName("should toggle chain status from alive to dead")
        void shouldToggleChainStatusFromAliveToDead() {
            ChainInfo chainInfo = createTestChainInfo(1, StoneColor.BLACK);
            state.addChainInfo(chainInfo);
            
            assertEquals(DeadStoneStatus.ALIVE, chainInfo.getStatus());
            state.toggleChainStatus(1);
            assertEquals(DeadStoneStatus.DEAD, chainInfo.getStatus());
        }

        @Test
        @DisplayName("should toggle chain status from dead to alive")
        void shouldToggleChainStatusFromDeadToAlive() {
            ChainInfo chainInfo = createTestChainInfo(1, StoneColor.BLACK);
            chainInfo.setStatus(DeadStoneStatus.DEAD);
            state.addChainInfo(chainInfo);
            
            state.toggleChainStatus(1);
            assertEquals(DeadStoneStatus.ALIVE, chainInfo.getStatus());
        }

        @Test
        @DisplayName("should reset acceptance when toggling")
        void shouldResetAcceptanceWhenToggling() {
            ChainInfo chainInfo = createTestChainInfo(1, StoneColor.BLACK);
            state.addChainInfo(chainInfo);
            state.setBlackAccepted(true);
            state.setWhiteAccepted(true);
            
            state.toggleChainStatus(1);
            
            assertFalse(state.isBlackAccepted());
            assertFalse(state.isWhiteAccepted());
        }

        @Test
        @DisplayName("should return false for non-existent chain")
        void shouldReturnFalseForNonExistentChain() {
            assertFalse(state.toggleChainStatus(999));
        }
    }

    @Nested
    @DisplayName("Dead Stone Counting Tests")
    class DeadStoneCountingTests {

        @Test
        @DisplayName("should count dead stones for color")
        void shouldCountDeadStonesForColor() {
            // Create chains with multiple stones
            Stone stone1 = new Stone(new Position(0, 0), StoneColor.BLACK);
            Stone stone2 = new Stone(new Position(0, 1), StoneColor.BLACK);
            Chain chain1 = new Chain(stone1, StoneColor.BLACK);
            chain1.addStone(stone2);
            ChainInfo blackChain = new ChainInfo(1, chain1, 2);
            blackChain.setStatus(DeadStoneStatus.DEAD);
            
            Stone stone3 = new Stone(new Position(5, 5), StoneColor.WHITE);
            Chain chain2 = new Chain(stone3, StoneColor.WHITE);
            ChainInfo whiteChain = new ChainInfo(2, chain2, 4);
            whiteChain.setStatus(DeadStoneStatus.ALIVE);
            
            state.addChainInfo(blackChain);
            state.addChainInfo(whiteChain);
            
            assertEquals(2, state.countDeadStones(StoneColor.BLACK));
            assertEquals(0, state.countDeadStones(StoneColor.WHITE));
        }

        @Test
        @DisplayName("should return zero when no dead stones")
        void shouldReturnZeroWhenNoDeadStones() {
            assertEquals(0, state.countDeadStones(StoneColor.BLACK));
            assertEquals(0, state.countDeadStones(StoneColor.WHITE));
        }
    }
}
