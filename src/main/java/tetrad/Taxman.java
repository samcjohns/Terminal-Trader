package tetrad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Scanner;

import static tetrad.Mutil.MENU_WIDTH;
import static tetrad.Mutil.clearLine;
import static tetrad.Mutil.clearScreen;
import static tetrad.Mutil.cursorUp;
import static tetrad.Mutil.pause;

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
    private double   bribePrice; // bribing price for the taxman to spare user
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
        this.cooldown = 1;
        this.bribePrice = 100;
    }

    /**
     * Main method for the taxman, will clear the screen and handle a dialogue
     * with the player. If it is not time yet, method will return immediately.
     */
    public void visit(Scanner scanner) {
        // check if wrong day
        if (game.cldr.monthsBetween(lastVisit) < cooldown || !game.cldr.isFirstDayOfMonth()) {
            return; // don't visit today
        }

        // right day, do visit
        clearScreen();
        Game.printHeader();
        printTaxman();
        System.out.println("-".repeat(MENU_WIDTH));
        System.out.println("\n" + "-".repeat(MENU_WIDTH));
        cursorUp(2);
        subtitlePrint("", cooldown);

        
        bribePrice = sigRound(user.getTaxData()/10 + bribePrice, 2);
        
    }

    /**
     * Prints ASCII art for the Taxman
     */
    private static void printTaxman() {
        String filePath = Main.getSource("assets");
        filePath += "taxman70.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("|" + line + "|");
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
            pause(100); // Use the pause method for the delay.
        }
        
        pause(delay); // Pause briefly after printing the message for dramatic effect.
        
        // Clear the line after the message is fully displayed.
        clearLine();
    }
}
