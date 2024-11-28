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
import static tetrad.Mutil.cyan;
import static tetrad.Mutil.green;
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

    private final int alertsPerPage = 12; // number of alerts per page in news feed

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

            // Check if adding the next headline would overflow the MENU_WIDTH
            if (currentLength + additionalLength > MENU_WIDTH) {
                // If it would, truncate and exit the method
                appendTruncated(line, headline, currentLength, separator, alert.getType());
                System.out.println("Breaking after truncating headline: " + headline);  // Debugging line
                return;
            }

            // Append the headline with the separator, update current length after appending
            line.append(separator).append(headlineColor(headline, alert.getType()));
            currentLength += additionalLength;
        }

        // If space is left after the reel, fill with random headlines
        while (currentLength < MENU_WIDTH) {
            String headline = randomHeadline();
            String separator = currentLength > "News: ".length() ? " | " : "";
            int additionalLength = separator.length() + headline.length();

            // Check if adding the random headline will overflow the MENU_WIDTH
            if (currentLength + additionalLength > MENU_WIDTH) {
                appendTruncated(line, headline, currentLength, separator, -1); // Random headlines have no type color
                break;
            }

            // Append random headline and update current length
            line.append(separator).append(magenta(headline));
            currentLength += additionalLength;
        }

        // Finally, print the built line
        System.out.println(line.toString());
    }


    /**
     * Prints the specified page of current events.
     *
     * @param page The page number to display (1-based index).
     */
    public void page(int page) {
        int alertsPerPage = 12;
        int totalPages = (int) Math.ceil(reel.size() / (double) alertsPerPage);

        // Check if the page number is within the valid range (1-based index)
        if (page < 1 || page > totalPages) {
            return;
        }

        // Convert 1-based page number to 0-based index for internal processing
        int zeroBasedPage = page - 1;

        // Display the specified page of alerts
        for (int i = zeroBasedPage * alertsPerPage; i < Math.min(reel.size(), (zeroBasedPage + 1) * alertsPerPage); i++) {
            int age = reel.get(i).getAge();
            switch (age) {
                case 0 -> System.out.print(cyan("Today: "));
                case 1 -> System.out.print(cyan("Yesterday: "));
                default -> System.out.print(cyan(age + " Days Ago: "));
            }
            System.out.println(headlineColor(reel.get(i).getHeadline(), reel.get(i).getType()) + "\n");
        }

        // Display footer with page number (1-based index)
        System.out.println("-".repeat(MENU_WIDTH));
        System.out.println("Page " + page + " of " + totalPages);
        System.out.println("-".repeat(MENU_WIDTH));
    }


    /**
     * @return number of pages in the News Feed
     */
    int pages() {
        return reel.size() / alertsPerPage + 1;
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