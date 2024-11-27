package tetrad;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import static tetrad.Alert.ACHIEVEMENT;
import static tetrad.Alert.ALERT_MARKET;
import static tetrad.Alert.ALERT_STOCK;
import static tetrad.Alert.BAD_MARKET;
import static tetrad.Alert.BAD_STOCK;
import static tetrad.Alert.GOOD_MARKET;
import static tetrad.Alert.GOOD_STOCK;
import static tetrad.Mutil.MENU_WIDTH;
import static tetrad.Mutil.blue;
import static tetrad.Mutil.bold;
import static tetrad.Mutil.center;
import static tetrad.Mutil.cyan;
import static tetrad.Mutil.green;
import static tetrad.Mutil.italic;
import static tetrad.Mutil.magenta;
import static tetrad.Mutil.red;
import static tetrad.Mutil.yellow;

/**
 * News class for holding, preparing, and displaying Alert events
 * 
 * @author Samuel Johns
 * Created: November 15, 2024
 * 
 * Description: The News class is designed to hold alerts and report them
 *              properly. It interprets the Alerts sent by the various classes
 *              and displays stories for the player to predict stock behavior.
 *              It is responsible for keeping track of Alerts, their age, and
 *              their type. The News class producing a "headline" string to 
 *              displaying the most important events and a "page" of events
 *              that TerminalGame prints to screen.
 * 
 * @see Alert
 */

public class News {
    LinkedList<Alert> reel; // current alerts to be posted

    News() {
        this.reel = new LinkedList<>();
    }

    /**
     * Clears all alerts in the reel.
     */
    void clear() {
        reel.clear();
    }

    /**
     * Adds a new Alert to the reel.
     */
    void push(Alert newAlert) {
        reel.addFirst(newAlert);
    }

    /**
     * Prints a single line of the most important headlines.
     */
    void roll() {
        StringBuilder line = new StringBuilder(bold(cyan("News: ")));
        int currentLength = "News: ".length();

        // Add reel headlines until space is exhausted or reel is empty
        for (Alert alert : reel) {
            String headline = alert.getHeadline();
            String separator = currentLength > "News: ".length() ? " | " : "";
            int additionalLength = separator.length() + headline.length();

            if (currentLength + additionalLength > MENU_WIDTH) {
                appendTruncated(line, headline, currentLength, separator, alert.getType());
                return;
            }

            // Append colored headline with separator
            line.append(separator).append(headlineColor(headline, alert.getType()));
            currentLength += additionalLength;
        }

        // If space is left after reel, fill with random headlines
        while (currentLength < MENU_WIDTH) {
            String headline = randomHeadline();
            String separator = currentLength > "News: ".length() ? " | " : "";
            int additionalLength = separator.length() + headline.length();

            if (currentLength + additionalLength > MENU_WIDTH) {
                appendTruncated(line, headline, currentLength, separator, -1); // Random headlines have no type color
                break;
            }

            line.append(separator).append(magenta(headline));
            currentLength += additionalLength;
        }

        System.out.println(line.toString());
    }

    /**
     * Prints a whole page containing all current events.
     */
    void page() {
        System.out.println(""); // Spacing
        System.out.println(italic(center("Mind the dust...", MENU_WIDTH)));
        System.out.println(""); // Spacing
        for (int i = 0; i < reel.size(); i++) {
            int age = reel.get(i).getAge();
            switch (age) {
                case 0 -> System.out.print(cyan("Today: "));
                case 1 -> System.out.print(cyan("Yesterday: "));
                default -> System.out.print(cyan(age + " Days Ago: "));
            }
            System.out.println(headlineColor(reel.get(i).getHeadline(), reel.get(i).getType()) + "\n");
        }
        clear(); // Clear list
    }

    /**
     * Updates the age of alerts after each advance.
     */
    void update() {
        for (int i = 0; i < reel.size(); i++) {
            reel.get(i).incrementAge();
        }
    }

    /**
     * Returns colored headlines based on the alert type.
     * @return headline in the proper color
     */
    private String headlineColor(String headline, int type) {
        return switch (type) {
            case ACHIEVEMENT -> bold(blue(headline));
            case BAD_STOCK -> red(headline);
            case GOOD_STOCK -> green(headline);
            case ALERT_STOCK -> yellow(headline);
            case BAD_MARKET -> bold(red(headline));
            case GOOD_MARKET -> bold(green(headline));
            case ALERT_MARKET -> bold(yellow(headline));
            default -> headline; // No color
        };
    }

    /**
     * Searches "random_headlines.txt" and returns a random one.
     * @return string containing a random headline
     */
    private String randomHeadline() {
        final int RHF_LENGTH = 46; // May change, file length
        Random random = new Random();
        int randomLineIndex = random.nextInt(RHF_LENGTH);

        try (Scanner scanner = new Scanner(new File("assets/random_headlines.txt"))) {
            for (int i = 0; i < randomLineIndex; i++) {
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }
            }
            return scanner.hasNextLine() ? scanner.nextLine() : "No random headline available";
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
            return "Missing 'random_headlines.txt' asset";
        }
    }

     /**
     * Appends a truncated headline with a separator if necessary.
     */
    private void appendTruncated(StringBuilder line, String headline, int currentLength, String separator, int type) {
        int remainingLength = MENU_WIDTH - currentLength - separator.length() - 3; // Leave space for "..."
        if (remainingLength > 0) {
            int cutOffIndex = Math.min(remainingLength, headline.length());
            while (cutOffIndex > 0 && headline.charAt(cutOffIndex - 1) != ' ') {
                cutOffIndex--;
            }
            if (cutOffIndex == 0) cutOffIndex = remainingLength;

            // Append truncated headline with "..."
            String truncStr = headline.substring(0, cutOffIndex);
            truncStr = type == -1 ? magenta(truncStr) : headlineColor(truncStr, type);
            line.append(separator).append(truncStr).append("...");
        }
    }
}