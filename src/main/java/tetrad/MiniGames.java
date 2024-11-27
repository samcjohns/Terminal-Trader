package tetrad;

import java.util.Random;
import java.util.Scanner;

import static tetrad.Mutil.clearLine;
import static tetrad.Mutil.clearScreen;
import static tetrad.Mutil.dollar;
import static tetrad.Mutil.header;
import static tetrad.Mutil.pause;

/**
 * Class for assorted static mini game methods
 * 
 * @author Samuel Johns
 * Created: November 26, 2024
 */
public class MiniGames {
    /**
     * Gets user input to load a desired profile and take a wager from the
     * user. Will take the cash from the user's account and return that value
     * as a double. User save loads from file.
     * @param scanner user input scanner
     * @return amount of money the user would like to wager
     */
    public static double userWager(Scanner scanner) {
        // loop until valid outcome
        while (true) {
            System.out.println("Username: ");
            String un = scanner.nextLine();
            User usr = new User();
            try {
                usr.load(un);

                clearLine();
                System.out.print("Amount to wager (Max = " + usr.getCash() + "): ");
                double wager = Double.parseDouble(scanner.nextLine());

                if (wager > 0 && wager <= usr.getCash()) {
                    usr.setCash(usr.getCash() - wager);
                    usr.save();
                    return wager;
                }
                else {
                    System.out.println("Invalid Amount");
                }
            }
            catch (InitException e) {
                System.out.println(e.getMessage());
                pause(scanner);
                clearLine();
            }
            catch (NumberFormatException e) {
                clearLine();
                System.out.println("Invalid Input");
                pause(scanner);
            }
        }
    }

    /**
     * Returns cash back to the user after a mini game 
     * @param cashout cash to be returned
     */
    public static void userCashout(Scanner scanner, double cashout) {
        User usr = new User();
        // loop until valid outcome
        while (true) {
            System.out.println("Username: ");
            String un = scanner.nextLine();
            try {
                usr.load(un);
                break;
            }
            catch (InitException e) {
                System.out.println("Invalid Account");
                pause(scanner);
                clearLine(2);
            }
        }
        usr.setCash(usr.getCash() + cashout);
        usr.save();
    }

    /**
     * Coin toss minigame, the player makes a wager, and there is a 50/50
     * chance they double their wager or lose it all.
     * @param scanner user input scanner
     * @param cash amount of money to brought into the game
     * @return cash after playing the game
     */
    public static void coinToss(Scanner scanner) {
        // amount of cash to be returned
        double cashout = userWager(scanner);

        while (true) {
            clearScreen();
            header("Coin Toss");
            System.out.println("Cashout Amount: " + dollar(cashout));

            // get wager
            double wager = 0;
            System.out.print("\nPlease enter a wager: ");
            try {
                wager = Double.parseDouble(scanner.nextLine());
                cashout -= wager; // take wager from cash
            } 
            catch (NumberFormatException e) {
                clearLine();
                System.out.println("Invalid wager");
                pause(scanner);
            }
            
            // calculate win or lose
            Random rand = new Random();
            if (rand.nextBoolean()) {
                // win case
                System.out.println("You win! +" + dollar(wager * 2));
                cashout += wager * 2; // return double wager
                pause(1000);
            }
            else {
                // lose case
                System.out.println("You lost! -" + dollar(wager));
                pause(1000);
            }

            // loop for valid answer
            INNER:
            while (true) {
                System.out.println("Continue? Y/N");
                String choice = scanner.nextLine();
                switch (choice.toUpperCase()) {
                    case "N" -> {
                        userCashout(scanner, cashout);
                        return;
                    }
                    case "Y" -> {
                        // play again
                        break INNER;
                    }
                    default -> {
                        System.out.println("Invalid Input");
                        pause(scanner);
                        clearLine(2);
                    }
                }
            }
        }
    }
}