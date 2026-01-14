package com.gogame.domain.model;

import com.gogame.domain.enums.DeadStoneStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the state of scoring negotiation between players.
 * Tracks which chains are marked as dead and player acceptance status.
 */
public class NegotiationState {
    private final Map<Integer, ChainInfo> chainInfoMap;
    private boolean blackAccepted;
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
