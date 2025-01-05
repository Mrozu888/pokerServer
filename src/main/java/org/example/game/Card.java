package org.example.game;

import lombok.Data;

enum Rank {
    TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE
}

// Enum representing the suit of the card
enum Suit {
    HEARTS, DIAMONDS, CLUBS, SPADES
}

@Data

public class Card {
    private final Rank rank;
    private final Suit suit;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    @Override
    public String toString() {
        final String RESET = "\u001B[0m";
        final String RED = "\u001B[31m";
        final String BLACK = "\u001B[30m";

        // Map suits to symbols and colors
        String suitSymbol;
        String color = switch (suit) {
            case HEARTS -> {
                suitSymbol = "♥";
                yield RED;
            }
            case DIAMONDS -> {
                suitSymbol = "♦";
                yield RED;
            }
            case CLUBS -> {
                suitSymbol = "♣";
                yield BLACK;
            }
            case SPADES -> {
                suitSymbol = "♠";
                yield BLACK;
            }
            default -> {
                suitSymbol = "?";
                yield RESET;
            }
        };

        // Map ranks to human-readable strings
        String rankString;
        switch (rank) {
            case TWO: rankString = "2"; break;
            case THREE: rankString = "3"; break;
            case FOUR: rankString = "4"; break;
            case FIVE: rankString = "5"; break;
            case SIX: rankString = "6"; break;
            case SEVEN: rankString = "7"; break;
            case EIGHT: rankString = "8"; break;
            case NINE: rankString = "9"; break;
            case TEN: rankString = "10"; break;
            case JACK: rankString = "J"; break;
            case QUEEN: rankString = "Q"; break;
            case KING: rankString = "K"; break;
            case ACE: rankString = "A"; break;
            default: rankString = "?";
        }

        return color + rankString + suitSymbol + RESET;
    }
}
