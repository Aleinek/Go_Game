package com.gogame.model;

public class Territory {

    private int blackTerritory;
    private int whiteTerritory;
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