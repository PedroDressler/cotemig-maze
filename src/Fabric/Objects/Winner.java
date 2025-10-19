package Fabric.Objects;

import Fabric.Objects.Rat.Rat;

public class Winner {
    private Rat winner;

    public Winner() {}

    public Rat getWinner() {
        return winner;
    }

    public synchronized void setWinner(Rat winner) {
    	if (this.winner == null) {
    		this.winner = winner;
    	}
    }
    
    @Override
    public String toString() {
        return "Vencedor: " + getWinner();
    }
    
}
