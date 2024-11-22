package tetrad;

import static tetrad.Mutil.MENU_WIDTH;
import static tetrad.Mutil.blue;
import static tetrad.Mutil.bold;
import static tetrad.Mutil.center;
import static tetrad.Mutil.cyan;
import static tetrad.Mutil.italic;
import static tetrad.Mutil.magenta;

public class TerminalNews extends News {
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
    protected void printEmptyNewsRoll(StringBuilder line) {
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
