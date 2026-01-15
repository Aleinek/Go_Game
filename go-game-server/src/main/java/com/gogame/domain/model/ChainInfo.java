package com.gogame.domain.model;

import com.gogame.domain.enums.DeadStoneStatus;
import com.gogame.domain.enums.StoneColor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents information about a chain during the negotiation (scoring) phase.
 * <p>
 * Contains:
 * <ul>
 *   <li>Unique chain identifier</li>
 *   <li>Positions of all stones in the chain</li>
 *   <li>Stone color</li>
 *   <li>Dead/Alive status (can be toggled during negotiation)</li>
 *   <li>Liberty count</li>
 * </ul>
 * </p>
 * <p>
 * During negotiation, players can mark chains as DEAD or ALIVE.
 * Dead chains count as prisoners for the opponent's score.
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class ChainInfo {
    /** Unique identifier for this chain during negotiation. */
    private final int chainId;
    /** Set of board positions occupied by this chain's stones. */
    private final Set<Position> positions;
    /** Color of stones in this chain. */
    private final StoneColor color;
    /** Whether this chain is marked as DEAD or ALIVE. */
    private DeadStoneStatus status;
    /** Number of liberties this chain has. */
    private final int liberties;

    public ChainInfo(int chainId, Chain chain, int liberties) {
        this.chainId = chainId;
        this.positions = new HashSet<>();
        for (Stone stone : chain.getStones()) {
            this.positions.add(stone.getPosition());
        }
        this.color = chain.getStones().iterator().next().getColor();
        this.status = DeadStoneStatus.ALIVE;
        this.liberties = liberties;
    }

    public int getChainId() {
        return chainId;
    }

    public Set<Position> getPositions() {
        return positions;
    }

    public StoneColor getColor() {
        return color;
    }

    public DeadStoneStatus getStatus() {
        return status;
    }

    public void setStatus(DeadStoneStatus status) {
        this.status = status;
    }

    public int getLiberties() {
        return liberties;
    }

    public int getStoneCount() {
        return positions.size();
    }

    public void toggleStatus() {
        this.status = (this.status == DeadStoneStatus.ALIVE) 
            ? DeadStoneStatus.DEAD 
            : DeadStoneStatus.ALIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChainInfo chainInfo = (ChainInfo) o;
        return chainId == chainInfo.chainId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chainId);
    }
}
