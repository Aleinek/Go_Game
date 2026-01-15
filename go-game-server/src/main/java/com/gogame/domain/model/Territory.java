package com.gogame.domain.model;

/**
 * Represents the territory counts on a Go board.
 * <p>
 * Territory is calculated using flood-fill from empty intersections.
 * An empty region belongs to a player if it is surrounded only by
 * that player's stones. Regions touching both colors are neutral (dame).
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class Territory {
    /** Number of intersections controlled by black. */
    private int blackTerritory;
    /** Number of intersections controlled by white. */
    private int whiteTerritory;
    /** Number of neutral points (dame). */
    private int neutralTerritory;
    
    public Territory(int size) {
        this.blackTerritory = 0;
        this.whiteTerritory = 0;
        this.neutralTerritory = size*size;
    }
    
    public int getWhiteTerritory() {
        return whiteTerritory;
    }
    
    public int getNeutralTerritory() {
        return neutralTerritory;
    }

    public int getBlackTerritory() {
        return blackTerritory;
    }

    public void setBlackTerritory(int blackTerritory) {
        this.blackTerritory = blackTerritory;
    }

    public void setNeutralTerritory(int neutralTerritory) {
        this.neutralTerritory = neutralTerritory;
    }
    public void setWhiteTerritory(int whiteTerritory) {
        this.whiteTerritory = whiteTerritory;
    }
}