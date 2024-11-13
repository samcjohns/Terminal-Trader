package tetrad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

// FIXME this is all garbage right now
public class MiniGames {
    private User relevantUser;
    
    MiniGames(User relevantUser) {
        this.relevantUser = relevantUser;
    }

    public static void main(String[] args) {
        MiniGames gm = new MiniGames(new User("blank", 100.0, null, null));
        gm.playBlackjack();
    }

    public void playBlackjack() {
        // Initialize deck, hands, balance, and scanner
        List<String> deck = new ArrayList<>();
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};
        int[] values = {2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 11};
        for (String suit : suits)
            for (int i = 0; i < ranks.length; i++)
                deck.add(ranks[i] + " of " + suit + " (" + values[i] + ")");
        
        Collections.shuffle(deck);
        int balance = 100;  // Starting balance
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Blackjack! Your starting balance is $" + balance);

        // Betting loop
        while (balance > 0) {
            System.out.print("Enter your bet (Balance: $" + balance + "): ");
            int bet = Integer.parseInt(scanner.nextLine());

            if (bet > balance || bet <= 0) {
                System.out.println("Invalid bet amount. Please try again.");
                continue;
            }

            List<String> playerHand = new ArrayList<>();
            List<String> dealerHand = new ArrayList<>();
            int playerTotal = 0, dealerTotal = 0;
            
            // Initial deal
            playerTotal += drawCard(deck, playerHand);
            playerTotal += drawCard(deck, playerHand);
            dealerTotal += drawCard(deck, dealerHand);
            dealerTotal += drawCard(deck, dealerHand);

            // Player's turn
            while (true) {
                System.out.println("Player's hand: " + playerHand + " (Total: " + playerTotal + ")");
                if (playerTotal == 21) {
                    System.out.println("Blackjack! You win!");
                    balance += bet;
                    break;
                } else if (playerTotal > 21) {
                    System.out.println("Bust! You lose.");
                    balance -= bet;
                    break;
                }
                System.out.print("Would you like to (H)it or (S)tand? ");
                if (scanner.nextLine().toLowerCase().equals("s")) break;
                playerTotal += drawCard(deck, playerHand);
            }

            // Dealer's turn
            if (playerTotal <= 21) {
                while (dealerTotal < 17) {
                    dealerTotal += drawCard(deck, dealerHand);
                }
                System.out.println("Dealer's hand: " + dealerHand + " (Total: " + dealerTotal + ")");
                
                if (dealerTotal > 21 || playerTotal > dealerTotal) {
                    System.out.println("You win!");
                    balance += bet;
                } else if (playerTotal < dealerTotal) {
                    System.out.println("Dealer wins.");
                    balance -= bet;
                } else {
                    System.out.println("It's a tie!");
                }
            }
            
            if (balance <= 0) {
                System.out.println("You're out of money! Game over.");
                break;
            }
            
            System.out.print("Play again? (y/n): ");
            if (!scanner.nextLine().toLowerCase().equals("y")) break;
            Collections.shuffle(deck); // Reshuffle for a new game
        }

        System.out.println("Game over! Your final balance is $" + balance);
    }

    private int drawCard(List<String> deck, List<String> hand) {
        if (deck.isEmpty()) {
            System.out.println("Shuffling new deck...");
            Collections.shuffle(deck); // Reshuffle deck if empty
        }
        String card = deck.remove(0);
        int value = Integer.parseInt(card.replaceAll("\\D+", ""));
        hand.add(card);
        return value;
    }
}