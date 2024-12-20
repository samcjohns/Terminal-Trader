package tetrad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;
import java.util.Scanner;

import static tetrad.ScreenBuilder.MENU_WIDTH;
import static tetrad.Mutil.blue;
import static tetrad.Mutil.center;
import static tetrad.Mutil.clearScreen;
import static tetrad.Mutil.cursorUp;
import static tetrad.Mutil.cyan;
import static tetrad.Mutil.dollar;
import static tetrad.Mutil.pause;
import static tetrad.Mutil.red;
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

    // default values
    private static    int   DEFAULT_COOLDOWN = 3;
    private static double DEFAULT_BRIBEPRICE = 100;
    private static double       DEFAULT_RATE = 0.3;

    /**
     * Creates a new Taxman for this game instance
     * @param game this game instance
     */
    public Taxman(Game game) {
        this.game = game;
        this.user = game.usr;
        this.lastVisit = game.cldr.getToday();
        
        // default values
        this.cooldown = DEFAULT_COOLDOWN;
        this.bribePrice = DEFAULT_BRIBEPRICE;
        this.rate = DEFAULT_RATE;
        this.lastTax = 0;
    }

    /**
     * Main method for the taxman, will clear the screen and handle a dialogue
     * with the player. If it is not time yet, method will return immediately.
     * @param scanner user input scanner
     * @param override true to override visit logic and force visit
     */
    public void visit(Scanner scanner, boolean override) {
        double gains = user.getTaxData();
        if (!override) {
            // check if wrong day
            if (game.cldr.monthsBetween(lastVisit) < cooldown || !game.cldr.isFirstDayOfMonth()) {
                return; // don't visit today
            }

            // check if no taxes
            if (gains == 0) {
                return;
            }
        }

        // calculate values
        double tax = rate * gains;
        bribePrice = sigRound((gains + bribePrice)/10, 2);

        // right day, do visit
        clearScreen();
        Game.printHeader();
        printTaxman();
        System.out.println(yellow("-".repeat(MENU_WIDTH)));
        System.out.println();
        System.out.println(yellow("-".repeat(MENU_WIDTH)));
        cursorUp(2);

        subtitlePrint("Hello " + user.getName() + ". ", 2000);

        if (tax > lastTax) {
            subtitlePrint("I see you have been doing well for yourself... ", 2000);
        }
        else {
            subtitlePrint("I see you've had a rough month... ", 2000);
        }

        subtitlePrint("I've been looking at your account, and you seem to owe us " + dollar(tax), 3000);
        subtitlePrint("How would you like to pay?", 2000);
        
        boolean prison   = false;
        while (true) {
            System.out.print(blue("[/] - Pay Taxes | [.] - Bribe Taxman | [,] - Refuse | ['] - Request Delay | ") 
                        + yellow("Cash: " + dollar(user.getCash())) + " | Choose: ");
            boolean again = false;
            String input = scanner.nextLine().toUpperCase();
            switch (input) {
                case "/" -> {
                    // pay taxes
                    cursorUp(1);
                    System.out.print("\r" + " ".repeat(MENU_WIDTH) + "\r");
                    if (user.getCash() < tax) {
                        subtitlePrint("Uh oh! Should have been more prepared " + user.getName() + ". ", 1000);
                        subtitlePrint("You can't afford your taxes. ", 2000);
                        prison = true;
                    }
                    else {
                        user.setCash(user.getCash() - bribePrice);
                        subtitlePrint("Thank your for supporting your local goverment. ", 2000);
                        subtitlePrint("Goodbye. ", 1000);
                        bribePrice = 0; // reset bribe price
                    }
                }
                case "." -> {
                    // bribe taxman
                    cursorUp(1);
                    System.out.print("\r" + " ".repeat(MENU_WIDTH));
                    System.out.print("\r");
                    System.out.print(red("Offer Amount: "));
                    double bribe = 0;
                    input = scanner.nextLine();
                    cursorUp(1);
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
                    if (rand.nextDouble() <= bribe/bribePrice) {
                        System.out.print("\r" + " ".repeat(MENU_WIDTH) + "\r");
                        subtitlePrint("Very well, until next time...", 2000);
                        user.setCash(user.getCash() - bribe);
                    }
                    // reject case
                    else {
                        System.out.print("\r" + " ".repeat(MENU_WIDTH) + "\r");
                        subtitlePrint("It would take much more than that to buy my integrity " + user.getName() + "...", 2000);
                        subtitlePrint("Perhaps prison will give you time to reconsider your morals...", 2000);
                        prison = true;
                    }
                }
                case "," -> {
                    cursorUp(1);
                    System.out.print("\r" + " ".repeat(MENU_WIDTH) + "\r");
                    subtitlePrint("There are only two inevitables in life. Death and taxes...", 2000);
                    again = true;
                }
                case "'" -> {
                    cursorUp(1);
                    System.out.print("\r" + " ".repeat(MENU_WIDTH) + "\r");
                    // already requested delay
                    if (cooldown != DEFAULT_COOLDOWN) {
                        subtitlePrint("I've already given you enough time... ", 1000);
                        if (user.getNet() < tax) {
                            subtitlePrint("I'm sorry, " + user.getName() + ". ", 1000);
                            subtitlePrint("No more second chances... ", 2000);
                            prison = true;
                        }
                        else {
                            subtitlePrint("I wish it didn't come to this... ", 2000);
                            subtitlePrint("... ", 2000);
                            userLiquidate();
                            subtitlePrint("I've liquidated your portfolio to cover your costs", 2000);
                            subtitlePrint("Please be prepared next time... ", 2000);
                            subtitlePrint("Goodbye. ", 1000);
                        }
                        cooldown = DEFAULT_COOLDOWN;
                    }
                    else {
                        subtitlePrint("You have one month, please be prepared... ", 2000);
                        cooldown = 1;
                    }
                }
                default -> {
                    cursorUp(1);
                    System.out.print("\r" + " ".repeat(MENU_WIDTH) + "\r");
                    subtitlePrint("I didn't quite hear that...", 1000);
                    again = true;
                }
            }
            if (!again) {
                break;
            }
        }

        if (prison) {
            // sell all stocks in porfolio
            userLiquidate();

            // apply fine
            user.setCash(user.getCash() / 100);

            // determine sentence by wealth
            int sentence = (int) user.getNet() / 10000;
            Game.DO_TAXMAN = false; // disable taxman
            game.advance(sentence);
            Game.DO_TAXMAN = true; // reenable taxman

            Alert alert = new Alert("You fulfilled your " + sentence + " day sentence.", Alert.HOLIDAY, game.cldr.getToday());
            game.news.push(alert);
        }

        clearScreen();
        lastTax = tax;
        lastVisit = game.cldr.getToday();
    }

    /**
     * Main method for the taxman, will clear the screen and handle a dialogue
     * with the player. If it is not time yet, method will return immediately.
     * @param scanner user input scanner
     */
    public void visit(Scanner scanner) {
        this.visit(scanner, false);
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
                System.out.println(cyan("|") + center(line, MENU_WIDTH -2) + cyan("|"));
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

    /**
     * Sells all stocks in the users portfolio
     */
    private void userLiquidate() {
        Portfolio pf = user.getPortfolio();
        for (int i = 0; i < pf.size(); i++) {
            try {
                int amount = pf.amountAt(i);
                Stock stock = pf.stockAt(i);
                user.sell(stock, amount);
            }
            catch (InvalidSelectionException e) {
                // move on
            }
        }
    }
}
