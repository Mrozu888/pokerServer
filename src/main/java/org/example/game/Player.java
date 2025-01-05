package org.example.game;

import lombok.Data;

import java.util.List;


@Data
public class Player {
    private static int idCounter = 0;

    private Hand hand;
    private long money;
    private long bet;
    private String name;
    private State state;
    private int id;

    public Player( String name, long money) {
        this.id = idCounter++;
        this.name = name;
        this.money = money;
        this.state = State.LOBBY;
        this.bet = 0;
    }

    public void drawCards(List<Card> cards) {
        this.hand = new Hand(cards);
    }

    public void exchangeCards(int[] indexes, List<Card> newCards) {
        this.hand.exchangeCards(indexes, newCards);
        this.state = State.EXCHANGE;
    }

    public void addWinnings(long amount){
        this.money += amount;
    }

    public int[] getHandValues(){
        return this.hand.getEvaluatedValues();
    }


    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", bet=" + bet +
                ", state=" + state +
                ", cards=" + hand +
                '}';
    }
    public String toStringWithoutCards() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", bet=" + bet +
                ", state=" + state +
                '}';
    }
}
