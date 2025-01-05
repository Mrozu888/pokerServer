package org.example.game;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class Deck {
    private List<Card> cards;

    private Deck(List<Card> cards) {
        this.cards = cards;
    }

    public static Deck sortedDeck() {
        List<Card> cards = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
        return new Deck(cards);
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public List<Card> dealCards(int numberOfCards) {
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < numberOfCards; i++) {
            hand.add(cards.remove(0));
        }
        return hand;
    }
    public List<Card> getCards() {
        return cards;
    }
}
