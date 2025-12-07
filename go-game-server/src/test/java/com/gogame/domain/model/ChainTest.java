package com.gogame.domain.model;

import com.gogame.domain.enums.StoneColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Chain Tests")
class ChainTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(9);
    }

    @Nested
    @DisplayName("Constructor and Basic Operations")
    class ConstructorTests {

        @Test
        @DisplayName("should create chain with single stone")
        void shouldCreateChainWithSingleStone() {
            Stone stone = new Stone(new Position(3, 3), StoneColor.BLACK);
            Set<Stone> stones = new HashSet<>();
            stones.add(stone);
            
            Chain chain = new Chain(stones, StoneColor.BLACK);
            
            assertEquals(1, chain.getStones().size());
            assertTrue(chain.getStones().contains(stone));
        }

        @Test
        @DisplayName("should create chain with multiple stones")
        void shouldCreateChainWithMultipleStones() {
            Set<Stone> stones = new HashSet<>();
            stones.add(new Stone(new Position(3, 3), StoneColor.BLACK));
            stones.add(new Stone(new Position(3, 4), StoneColor.BLACK));
            stones.add(new Stone(new Position(4, 3), StoneColor.BLACK));
            
            Chain chain = new Chain(stones, StoneColor.BLACK);
            
            assertEquals(3, chain.getStones().size());
        }

        @Test
        @DisplayName("should create white chain")
        void shouldCreateWhiteChain() {
            Stone stone = new Stone(new Position(5, 5), StoneColor.WHITE);
            Set<Stone> stones = new HashSet<>();
            stones.add(stone);
            
            Chain chain = new Chain(stones, StoneColor.WHITE);
            
            assertEquals(StoneColor.WHITE, chain.color);
        }
    }

    @Nested
    @DisplayName("addStone()")
    class AddStoneTests {

        @Test
        @DisplayName("should add stone to existing chain")
        void shouldAddStoneToExistingChain() {
            Set<Stone> stones = new HashSet<>();
            stones.add(new Stone(new Position(3, 3), StoneColor.BLACK));
            Chain chain = new Chain(stones, StoneColor.BLACK);
            
            Stone newStone = new Stone(new Position(3, 4), StoneColor.BLACK);
            chain.addStone(newStone);
            
            assertEquals(2, chain.getStones().size());
            assertTrue(chain.getStones().contains(newStone));
        }

        @Test
        @DisplayName("should allow adding multiple stones")
        void shouldAllowAddingMultipleStones() {
            Set<Stone> stones = new HashSet<>();
            stones.add(new Stone(new Position(3, 3), StoneColor.BLACK));
            Chain chain = new Chain(stones, StoneColor.BLACK);
            
            chain.addStone(new Stone(new Position(3, 4), StoneColor.BLACK));
            chain.addStone(new Stone(new Position(4, 3), StoneColor.BLACK));
            chain.addStone(new Stone(new Position(2, 3), StoneColor.BLACK));
            
            assertEquals(4, chain.getStones().size());
        }
    }

    @Nested
    @DisplayName("getLiberties()")
    class GetLibertiesTests {

        @Test
        @DisplayName("should return 4 liberties for single stone in center")
        void shouldReturn4LibertiesForSingleStoneInCenter() {
            Position pos = new Position(4, 4);
            board.placeStone(pos, StoneColor.BLACK);
            Stone stone = board.getStoneAt(pos);
            
            Set<Stone> stones = new HashSet<>();
            stones.add(stone);
            Chain chain = new Chain(stones, StoneColor.BLACK);
            
            Set<Position> liberties = chain.getLiberties(board);
            
            assertEquals(4, liberties.size());
            assertTrue(liberties.contains(new Position(5, 4)));
            assertTrue(liberties.contains(new Position(3, 4)));
            assertTrue(liberties.contains(new Position(4, 5)));
            assertTrue(liberties.contains(new Position(4, 3)));
        }

        @Test
        @DisplayName("should return 2 liberties for single stone in corner")
        void shouldReturn2LibertiesForSingleStoneInCorner() {
            Position pos = new Position(0, 0);
            board.placeStone(pos, StoneColor.BLACK);
            Stone stone = board.getStoneAt(pos);
            
            Set<Stone> stones = new HashSet<>();
            stones.add(stone);
            Chain chain = new Chain(stones, StoneColor.BLACK);
            
            Set<Position> liberties = chain.getLiberties(board);
            
            assertEquals(2, liberties.size());
            assertTrue(liberties.contains(new Position(1, 0)));
            assertTrue(liberties.contains(new Position(0, 1)));
        }

        @Test
        @DisplayName("should return 3 liberties for single stone on edge")
        void shouldReturn3LibertiesForSingleStoneOnEdge() {
            Position pos = new Position(0, 4);
            board.placeStone(pos, StoneColor.BLACK);
            Stone stone = board.getStoneAt(pos);
            
            Set<Stone> stones = new HashSet<>();
            stones.add(stone);
            Chain chain = new Chain(stones, StoneColor.BLACK);
            
            Set<Position> liberties = chain.getLiberties(board);
            
            assertEquals(3, liberties.size());
        }

        @Test
        @DisplayName("should count shared liberties once for connected stones")
        void shouldCountSharedLibertiesOnceForConnectedStones() {
            // Two horizontally connected stones
            Position pos1 = new Position(4, 4);
            Position pos2 = new Position(5, 4);
            board.placeStone(pos1, StoneColor.BLACK);
            board.placeStone(pos2, StoneColor.BLACK);
            
            Set<Stone> stones = new HashSet<>();
            stones.add(board.getStoneAt(pos1));
            stones.add(board.getStoneAt(pos2));
            Chain chain = new Chain(stones, StoneColor.BLACK);
            
            Set<Position> liberties = chain.getLiberties(board);
            
            // Two horizontal stones share 6 unique liberties (not 8)
            assertEquals(6, liberties.size());
        }

        @Test
        @DisplayName("should reduce liberties when opponent stone placed adjacent")
        void shouldReduceLibertiesWhenOpponentStonePlacedAdjacent() {
            Position blackPos = new Position(4, 4);
            Position whitePos = new Position(5, 4);
            board.placeStone(blackPos, StoneColor.BLACK);
            board.placeStone(whitePos, StoneColor.WHITE);
            
            Set<Stone> stones = new HashSet<>();
            stones.add(board.getStoneAt(blackPos));
            Chain chain = new Chain(stones, StoneColor.BLACK);
            
            Set<Position> liberties = chain.getLiberties(board);
            
            assertEquals(3, liberties.size());
            assertFalse(liberties.contains(new Position(5, 4))); // occupied by white
        }
    }

    @Nested
    @DisplayName("isCaptured()")
    class IsCapturedTests {

        @Test
        @DisplayName("should not be captured when stone has liberties")
        void shouldNotBeCapturedWhenStoneHasLiberties() {
            Position pos = new Position(4, 4);
            board.placeStone(pos, StoneColor.BLACK);
            
            Set<Stone> stones = new HashSet<>();
            stones.add(board.getStoneAt(pos));
            Chain chain = new Chain(stones, StoneColor.BLACK);
            
            assertFalse(chain.isCaptured(board));
        }

        @Test
        @DisplayName("should be captured when single stone has no liberties in corner")
        void shouldBeCapturedWhenSingleStoneHasNoLibertiesInCorner() {
            // Place black stone in corner
            Position blackPos = new Position(0, 0);
            board.placeStone(blackPos, StoneColor.BLACK);
            
            // Surround with white stones
            board.placeStone(new Position(1, 0), StoneColor.WHITE);
            board.placeStone(new Position(0, 1), StoneColor.WHITE);
            
            Set<Stone> stones = new HashSet<>();
            stones.add(board.getStoneAt(blackPos));
            Chain chain = new Chain(stones, StoneColor.BLACK);
            
            assertTrue(chain.isCaptured(board));
        }

        @Test
        @DisplayName("should be captured when chain surrounded")
        void shouldBeCapturedWhenChainSurrounded() {
            // Create a 2-stone black chain
            Position pos1 = new Position(1, 1);
            Position pos2 = new Position(1, 2);
            board.placeStone(pos1, StoneColor.BLACK);
            board.placeStone(pos2, StoneColor.BLACK);
            
            // Surround with white stones
            board.placeStone(new Position(0, 1), StoneColor.WHITE);
            board.placeStone(new Position(0, 2), StoneColor.WHITE);
            board.placeStone(new Position(2, 1), StoneColor.WHITE);
            board.placeStone(new Position(2, 2), StoneColor.WHITE);
            board.placeStone(new Position(1, 0), StoneColor.WHITE);
            board.placeStone(new Position(1, 3), StoneColor.WHITE);
            
            Set<Stone> stones = new HashSet<>();
            stones.add(board.getStoneAt(pos1));
            stones.add(board.getStoneAt(pos2));
            Chain chain = new Chain(stones, StoneColor.BLACK);
            
            assertTrue(chain.isCaptured(board));
        }

        @Test
        @DisplayName("should not be captured when chain has one liberty")
        void shouldNotBeCapturedWhenChainHasOneLiberty() {
            // Create a 2-stone black chain
            Position pos1 = new Position(1, 1);
            Position pos2 = new Position(1, 2);
            board.placeStone(pos1, StoneColor.BLACK);
            board.placeStone(pos2, StoneColor.BLACK);
            
            // Surround with white stones but leave one liberty
            board.placeStone(new Position(0, 1), StoneColor.WHITE);
            board.placeStone(new Position(0, 2), StoneColor.WHITE);
            board.placeStone(new Position(2, 1), StoneColor.WHITE);
            board.placeStone(new Position(2, 2), StoneColor.WHITE);
            board.placeStone(new Position(1, 0), StoneColor.WHITE);
            // (1, 3) is left empty - one liberty remains
            
            Set<Stone> stones = new HashSet<>();
            stones.add(board.getStoneAt(pos1));
            stones.add(board.getStoneAt(pos2));
            Chain chain = new Chain(stones, StoneColor.BLACK);
            
            assertFalse(chain.isCaptured(board));
        }
    }

    @Nested
    @DisplayName("merge()")
    class MergeTests {

        @Test
        @DisplayName("should merge two chains of same color")
        void shouldMergeTwoChainsOfSameColor() {
            Set<Stone> stones1 = new HashSet<>();
            stones1.add(new Stone(new Position(3, 3), StoneColor.BLACK));
            Chain chain1 = new Chain(stones1, StoneColor.BLACK);
            
            Set<Stone> stones2 = new HashSet<>();
            stones2.add(new Stone(new Position(3, 4), StoneColor.BLACK));
            stones2.add(new Stone(new Position(3, 5), StoneColor.BLACK));
            Chain chain2 = new Chain(stones2, StoneColor.BLACK);
            
            Chain merged = chain1.merge(chain2);
            
            assertEquals(3, merged.getStones().size());
            assertEquals(StoneColor.BLACK, merged.color);
        }

        @Test
        @DisplayName("should throw exception when merging different colors")
        void shouldThrowExceptionWhenMergingDifferentColors() {
            Set<Stone> blackStones = new HashSet<>();
            blackStones.add(new Stone(new Position(3, 3), StoneColor.BLACK));
            Chain blackChain = new Chain(blackStones, StoneColor.BLACK);
            
            Set<Stone> whiteStones = new HashSet<>();
            whiteStones.add(new Stone(new Position(3, 4), StoneColor.WHITE));
            Chain whiteChain = new Chain(whiteStones, StoneColor.WHITE);
            
            assertThrows(IllegalArgumentException.class, () -> {
                blackChain.merge(whiteChain);
            });
        }

        @Test
        @DisplayName("merged chain should not affect original chains")
        void mergedChainShouldNotAffectOriginalChains() {
            Set<Stone> stones1 = new HashSet<>();
            stones1.add(new Stone(new Position(3, 3), StoneColor.BLACK));
            Chain chain1 = new Chain(stones1, StoneColor.BLACK);
            
            Set<Stone> stones2 = new HashSet<>();
            stones2.add(new Stone(new Position(3, 4), StoneColor.BLACK));
            Chain chain2 = new Chain(stones2, StoneColor.BLACK);
            
            Chain merged = chain1.merge(chain2);
            
            assertEquals(1, chain1.getStones().size());
            assertEquals(1, chain2.getStones().size());
            assertEquals(2, merged.getStones().size());
        }
    }
}
