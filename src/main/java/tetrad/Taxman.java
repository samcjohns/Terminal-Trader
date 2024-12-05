package tetrad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;
import java.util.Scanner;

import static tetrad.Mutil.MENU_WIDTH;
import static tetrad.Mutil.blue;
import static tetrad.Mutil.center;
import static tetrad.Mutil.clearScreen;
import static tetrad.Mutil.cursorUp;
import static tetrad.Mutil.dollar;
import static tetrad.Mutil.pause;
import static tetrad.Mutil.yellow;

/**
 * Class for Taxman feature added in 1.1
 * 
 * @author Samuel Johns
 * Created: December 4, 2024
 * Description: The Taxman class is designed to monitor the time and appear 
 *              periodically to charge taxes to the user. The Taxman has a 
 *              fluctuating bribe price based on previous bribes. He also
 *              stores the last time he has visited.
 */
public class Taxman {
    private LocalDate lastVisit; // last time visited the player
    private int        cooldown; // months taxman will wait before returning
    private double         rate; // amount of realized gains taken
    private double   bribePrice; // bribing price for the taxman to spare user
    private double      lastTax; // last amount taxed from the player
    private final Game game; // reference to the current game object
    private final User user; // player that the taxman visits

    /**
     * Creates a new Taxman for this game instance
     * @param game this game instance
     */
    public Taxman(Game game) {
        this.game = game;
        this.user = game.usr;
        this.lastVisit = game.cldr.getToday();
        
        // default values
        this.cooldown = 3;
        this.bribePrice = 100;
        this.lastTax = 0;
        this.rate = 0.3;
    }

    /**
     * Main method for the taxman, will clear the screen and handle a dialogue
     * with the player. If it is not time yet, method will return immediately.
     */
    public void visit(Scanner scanner) {
        // // check if wrong day
        // if (game.cldr.monthsBetween(lastVisit) < cooldown || !game.cldr.isFirstDayOfMonth()) {
        //     return; // don't visit today
        // }

        // double gains = user.getTaxData();

        // // check if no taxes
        // if (gains == 0) {
        //     return;
        // }

        double gains = 1254.34;

        // calculate values
        double tax = rate * gains;
        bribePrice = sigRound((gains + bribePrice)/10, 2);

        // right day, do visit
        clearScreen();
        Game.printHeader();
        printTaxman();
        System.out.println("-".repeat(MENU_WIDTH));
        System.out.println();
        System.out.println("-".repeat(MENU_WIDTH));
        cursorUp(2);

        subtitlePrint("Hello " + user.getName() + ".", 2000);

        if (tax > lastTax) {
            subtitlePrint("I see you have been doing well for yourself...", 2000);
        }
        else {
            subtitlePrint("I see you've had a rough month...", 2000);
        }

        subtitlePrint("I've been looking at your account, and you seem to owe us " + dollar(tax), 3000);
        System.out.print(blue("[/] - Pay Taxes | [.] - Bribe Taxman | [,] - Refuse | ") + yellow("Cash: " + dollar(user.getCash())));
        
        boolean prison   = false;
        while (true) {
            boolean again = false;
            String input = scanner.nextLine().toUpperCase();
            switch (input) {
                case "/" -> {
                    // pay taxes
                    cursorUp(1);
                    System.out.print("\r" + " ".repeat(MENU_WIDTH));
                    System.out.print("\r");
                    if (user.getCash() < tax) {
                        subtitlePrint("Uh oh! Should have been more prepared " + user.getName() + ". You can't afford your taxes.", 2000);
                        again = true;
                        prison = true;
                    }
                    else {
                        user.setCash(user.getCash() - bribePrice);
                        subtitlePrint("Thank your for supporting your local goverment.", 2000);
                        subtitlePrint("Goodbye.", 1000);
                    }
                }
                case "." -> {
                    // bribe taxman
                    cursorUp(1);
                    System.out.print("\r" + " ".repeat(MENU_WIDTH));
                    System.out.print("\r");
                    System.out.print("Offer Amount: ");
                    double bribe = 0;
                    input = scanner.nextLine();

                    // invalid bribe
                    try {
                        bribe = Double.parseDouble(input);
                    } 
                    catch (NumberFormatException e) {
                        System.out.print("Invalid Bribe");
                        again = true;
                        break;
                    }

                    // valid bribe
                    // random chance he accepts it based on amount
                    Random rand = new Random();
                    // accept case
                    if (rand.nextDouble() >= bribe/bribePrice) {
                        subtitlePrint("Very well, until next time...", 2000);
                        user.setCash(user.getCash() - bribe);
                    }
                    else {
                        subtitlePrint("It would take much more than that to be my integrity " + user.getName(), 2000);
                        subtitlePrint("Perhaps prison will give you time to reconsider your morals...", 2000);
                        prison = true;
                    }
                }
                default -> {
                    subtitlePrint("I didn't quite hear that...", 1000);
                    again = true;
                }
            }
            if (!again) {
                break;
            }
        }

        if (prison) {
            // determine sentence by wealth
            int sentence = (int) user.getNet() / 10000;
            game.advance(sentence);

            Alert alert = new Alert("You fulfilled your " + sentence + " day sentence.", Alert.HOLIDAY, game.cldr.getToday());
            game.news.push(alert);
        }
        clearScreen();
    }

    /**
     * Prints ASCII art for the Taxman
     */
    private static void printTaxman() {
        String filePath = Main.getSource("assets");
        filePath += "taxman.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("|" + center(line, MENU_WIDTH -2) + "|");
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    /**
     * Rounds a double to two significant figures, used for determining bribe
     */
    public static double sigRound(double value, int significantFigures) {
        if (value == 0) {
            return 0;
        }
        final double scale = Math.pow(10, significantFigures - Math.ceil(Math.log10(Math.abs(value))));
        return Math.round(value * scale) / scale;
    }    

    /**
     * Prints a string one character at a time for dramatic effect,
     * waits for a given time, clears the line using clearLine(), and returns.
     *
     * @param message The string to be printed.
     * @param delay   The delay in milliseconds before line is cleared.
     */
    private static void subtitlePrint(String message, int delay) {
        // Print each character one by one with a delay.
        for (char c : message.toCharArray()) {
            System.out.print(c);
            pause(50); // Use the pause method for the delay.
        }
        
        pause(delay); // Pause briefly after printing the message for dramatic effect.

        // Clear the line after the message is fully displayed.
        System.out.println("\r" + " ".repeat(MENU_WIDTH));
        System.out.println("\r");
        cursorUp(2);
    }
}
