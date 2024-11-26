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
 */

public class News {
    LinkedList<Alert> reel; // current alerts to be posted

    News() {
        this.reel = new LinkedList<>();
    }

    void clear() {
        reel.clear();
    }

    /** Adds a new Alert to the reel
     * @param newAlert is the Alert to be pushed to the reel
     */
    void push(Alert newAlert) {
        reel.addFirst(newAlert);
    }

    /** prints a single line of the most important headlines */
    void roll() {
        StringBuilder line = new StringBuilder(bold(cyan("News: ")));
        int currentLength = "News: ".length();

        // in case of no news
        if (reel.isEmpty()) {
            printEmptyNewsRoll(line);
            return;
        }
    
        for (Alert alert : reel) {
            String headline = alert.getHeadline();
            String separator = currentLength > "News: ".length() ? " | " : "";
            int additionalLength = separator.length() + headline.length();
    
            // Check if adding the full headline would exceed the MENU_WIDTH limit
            if (currentLength + additionalLength > MENU_WIDTH) {
                // Calculate remaining length for the partial headline, minus 3 for "..."
                int remainingLength = MENU_WIDTH - currentLength - separator.length() - 3;
    
                if (remainingLength > 0) {
                    // Find the cut-off index: the last space within the remaining length
                    int cutOffIndex = remainingLength;
    
                    // Move back to find the last space
                    while (cutOffIndex > 0 && headline.charAt(cutOffIndex - 1) != ' ') {
                        cutOffIndex--;
                    }
    
                    // If there's no space found, cut off at the maximum allowed length
                    if (cutOffIndex == 0) {
                        cutOffIndex = remainingLength; // Fallback to cutting off at remaining length
                    }

                    // color according to type
                    String truncStr = headline.substring(0, cutOffIndex);
                    truncStr = headlineColor(truncStr, alert.getType());

                    // Add the separator first if needed
                    line.append(separator).append(truncStr).append("...");
                }
                break; // Stop after adding the partial headline
            }
    
            // Apply color based on the alert type after length check
            headline = headlineColor(alert.getHeadline(), alert.getType());
    
            // Add the separator and the colored headline to the line
            line.append(separator).append(headline);
            currentLength += additionalLength;
        }
    
        System.out.println(line.toString());
    }

    /** prints a whole page containing all current events */
    void page() {
        System.out.println(""); // spacing
        System.out.println(italic(center("Mind the dust...", MENU_WIDTH)));
        System.out.println(""); // spacing
        for (int i = 0; i < reel.size(); i++) {
            int age = reel.get(i).getAge();
            switch (age) {
                case 0 -> System.out.print(cyan("Today: "));
                case 1 -> System.out.print(cyan("Yesterday: "));
                default -> System.out.print(cyan(age + " Days Ago: "));
            }
            System.out.println(headlineColor(reel.get(i).getHeadline(), reel.get(i).getType()) + "\n");
        }
        clear(); // clear list
    }

    /** updates news after each advance */
    void update() {
        // increment age of all alerts
        for(int i = 0; i < reel.size(); i++) {
            reel.get(i).incrementAge();
        }
    }

    // coloring depending on alert type
    private String headlineColor(String headline, int type) {
        return switch (type) {
            case ACHIEVEMENT -> bold(blue(headline));
            case BAD_STOCK -> red(headline);
            case GOOD_STOCK -> green(headline);
            case ALERT_STOCK -> yellow(headline);
            case BAD_MARKET -> bold(red(headline));
            case GOOD_MARKET -> bold(green(headline));
            case ALERT_MARKET -> bold(yellow(headline));
            default -> headline; // don't color
        }; 
    }
    // searches "random_headlines.txt" and returns random one
    private String randomHeadline() {
        final int RHF_LENGTH = 42; // may change, file length
        Random random = new Random();
        int randomLineIndex = random.nextInt(RHF_LENGTH);

        try (Scanner scanner = new Scanner(new File("assets/random_headlines.txt"))) {
            // Skip lines until reaching the random line index
            for (int i = 0; i < randomLineIndex; i++) {
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }
            }

            // Return the next line
            return scanner.hasNextLine() ? scanner.nextLine() : null;
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
            return "Missing 'random_headlines.txt' asset";
        }
    }
    private void printEmptyNewsRoll(StringBuilder line) {
        String emptyMsg = "No Current Headlines";
        int currentLength = "News: ".length() + emptyMsg.length();
        line.append(blue(emptyMsg));

        // add random headlines until its full
        while(true) {
            String headline = randomHeadline();
            String separator = currentLength > "News: ".length() ? " | " : "";
            int additionalLength = separator.length() + headline.length();
    
            // Check if adding the full headline would exceed the MENU_WIDTH limit
            if (currentLength + additionalLength > MENU_WIDTH) {
                // Calculate remaining length for the partial headline, minus 3 for "..."
                int remainingLength = MENU_WIDTH - currentLength - separator.length() - 3;
    
                if (remainingLength > 0) {
                    // Find the cut-off index: the last space within the remaining length
                    int cutOffIndex = remainingLength;
    
                    // Move back to find the last space
                    while (cutOffIndex > 0 && headline.charAt(cutOffIndex - 1) != ' ') {
                        cutOffIndex--;
                    }
    
                    // If there's no space found, cut off at the maximum allowed length
                    if (cutOffIndex == 0) {
                        cutOffIndex = remainingLength; // Fallback to cutting off at remaining length
                    }

                    // color according to type
                    String truncStr = magenta(headline.substring(0, cutOffIndex));

                    // Add the separator first if needed
                    line.append(separator).append(truncStr).append("...");
                }
                break; // Stop after adding the partial headline
            }
        }
        System.out.println(line.toString());
    }
}