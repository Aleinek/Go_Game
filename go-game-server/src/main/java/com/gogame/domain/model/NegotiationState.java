package com.gogame.domain.model;

import com.gogame.domain.enums.DeadStoneStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the state of the scoring negotiation phase between players.
 * <p>
 * During negotiation:
 * <ul>
 *   <li>Both players review chains and mark them as DEAD or ALIVE</li>
 *   <li>When a player changes a chain's status, acceptance resets</li>
 *   <li>Both players must accept for the game to end</li>
 *   <li>Either player can resume playing instead of accepting</li>
 * </ul>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class NegotiationState {
    /** Map of chain ID to ChainInfo for all chains on the board. */
    private final Map<Integer, ChainInfo> chainInfoMap;
    /** Whether black player has accepted the current dead/alive markings. */
    private boolean blackAccepted;
    /** Whether white player has accepted the current dead/alive markings. */
    private boolean whiteAccepted;

    public NegotiationState() {
        this.chainInfoMap = new HashMap<>();
        this.blackAccepted = false;
        this.whiteAccepted = false;
    }

    public void addChainInfo(ChainInfo chainInfo) {
        chainInfoMap.put(chainInfo.getChainId(), chainInfo);
    }

    public ChainInfo getChainInfo(int chainId) {
        return chainInfoMap.get(chainId);
    }

    public Map<Integer, ChainInfo> getAllChainInfos() {
        return chainInfoMap;
    }

    public boolean isBlackAccepted() {
        return blackAccepted;
    }

    public void setBlackAccepted(boolean blackAccepted) {
        this.blackAccepted = blackAccepted;
    }

    public boolean isWhiteAccepted() {
        return whiteAccepted;
    }

    public void setWhiteAccepted(boolean whiteAccepted) {
        this.whiteAccepted = whiteAccepted;
    }

    public void resetAcceptance() {
        this.blackAccepted = false;
        this.whiteAccepted = false;
    }

    public boolean bothAccepted() {
        return blackAccepted && whiteAccepted;
    }

    /**
     * Toggles the dead/alive status of a chain and resets acceptance.
     * @param chainId the chain to toggle
     * @return true if toggle was successful, false if chain not found
     */
    public boolean toggleChainStatus(int chainId) {
        ChainInfo chainInfo = chainInfoMap.get(chainId);
        if (chainInfo == null) {
            return false;
        }
        chainInfo.toggleStatus();
        resetAcceptance();
        return true;
    }

    /**
     * Counts total stones marked as dead for a given color.
     */
    public int countDeadStones(com.gogame.domain.enums.StoneColor color) {
        return chainInfoMap.values().stream()
            .filter(ci -> ci.getColor() == color && ci.getStatus() == DeadStoneStatus.DEAD)
            .mapToInt(ChainInfo::getStoneCount)
            .sum();
    }
}
