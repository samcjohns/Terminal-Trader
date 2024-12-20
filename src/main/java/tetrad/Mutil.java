package tetrad;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * A utility class for string formatting, coloring, number conversion, etc...
 * 
 * @author Samuel Johns
 * Created: November 15, 2024
 * 
 * Description: The Mutil class is for holding static variables, methods for 
 *              string formatting and coloring, number conversion, debug
 *              methods, and any other useful static methods.
 */

public class Mutil {

    /** default width of history graphs */
    public static final int HISTORY_LENGTH = 120;

    /** default height of history graphs */
    public static final int HISTORY_HEIGHT = 25;

    /**
     * Adds a second string to the first string at a start location with space
     * in between the two strings.
     * @param first the original string
     * @param second the string to be added
     * @param start where the second string begins
     * @return the formatted String together
     */
    public static String add(String first, String second, int start) {
        // (formatted add)
        // adds the second string to the first string at start location
        String output = "";
        output += first;

        int diff = start - output.length();
        output += " ".repeat(diff);

        output += second;
        return output;
    }

    /**
     * Returns a String of the number colored according to the value
     * @param value the double to be converted
     * @return a String of the number, red for below 0, green for above
     */
    public static String numColor(double value) {
        if (value >= 0) {
            return green(" " + value); // space for minus sign
        } 
        else {
            return red(value + "");
        }
    }

     /**
     * Returns a String of the number colored according to the value
     * @param value the int to be converted
     * @return a String of the number, red for below 0, green for above
     */
    public static String numColor(int value) {
        return numColor((double) value);
    }

     /**
     * Returns a String of the number colored according to the value (the
     * String will be parsed for a number)
     * @param value the String of the number to be converted
     * @return a String of the number, red for below 0, green for above
     */
    public static String numColor(String value) {
        try {
            double numericValue = Double.parseDouble(value);
            return numColor(numericValue);
        } catch (NumberFormatException e) {
            return "Invalid number format";
        }
    }

    /**
     * Returns the given String in italic using ANSI codes (increases real len)
     * @param str string to be converted
     * @return italic String (ANSI codes appended)
     */
    public static String italic(String str) {
        return "\033[3m" + str + "\033[0m";
    }

    /**
     * Returns the given String in bold using ANSI codes (increases real len)
     * @param str string to be converted
     * @return bold String (ANSI codes appended)
     */
    public static String bold(String str) {
        return "\033[1m" + str + "\033[0m";
    }

    // text colors
    /**
     * Returns the given String in red using ANSI codes (increases real len)
     * @param str string to be converted
     * @return red String (ANSI codes appended)
     */
    public static String red(String str) {
        return "\033[31m" + str + "\033[0m";
    }

    /**
     * Returns the given String in green using ANSI codes (increases real len)
     * @param str string to be converted
     * @return green String (ANSI codes appended)
     */
    public static String green(String str) {
        return "\033[32m" + str + "\033[0m";
    }

    /**
     * Returns the given String in blue using ANSI codes (increases real len)
     * @param str string to be converted
     * @return blue String (ANSI codes appended)
     */
    public static String blue(String str) {
        return "\033[34m" + str + "\033[0m";
    }

    /**
     * Returns the given String in yellow using ANSI codes (increases real len)
     * @param str string to be converted
     * @return yellow String (ANSI codes appended)
     */
    public static String yellow(String str) {
        return "\033[33m" + str + "\033[0m";
    }

    /**
     * Returns the given String in magenta using ANSI codes (increases real len)
     * @param str string to be converted
     * @return magenta String (ANSI codes appended)
     */
    public static String magenta(String str) {
        return "\033[35m" + str + "\033[0m";
    }

    /**
     * Returns the given String in cyan using ANSI codes (increases real len)
     * @param str string to be converted
     * @return cyan String (ANSI codes appended)
     */
    public static String cyan(String str) {
        return "\033[36m" + str + "\033[0m";
    }

    // text background colors
    /**
     * Returns the given String with a red background using ANSI codes 
     * (increases real length)
     * @param str string to be converted
     * @return red background String (ANSI codes appended)
     */
    public static String redB(String str) {
        return "\033[41m" + str + "\033[0m";
    }

    /**
     * Returns the given String with a green background using ANSI codes 
     * (increases real length)
     * @param str string to be converted
     * @return green background String (ANSI codes appended)
     */
    public static String greenB(String str) {
        return "\033[42m" + str + "\033[0m";
    }

    /**
     * Returns the given String with a blue background using ANSI codes 
     * (increases real length)
     * @param str string to be converted
     * @return blue background String (ANSI codes appended)
     */
    public static String blueB(String str) {
        return "\033[44m" + str + "\033[0m";
    }

    /**
     * Returns the given String with a yellow background using ANSI codes 
     * (increases real length)
     * @param str string to be converted
     * @return yellow background String (ANSI codes appended)
     */
    public static String yellowB(String str) {
        return "\033[43m" + str + "\033[0m";
    }

    /**
     * Returns the given String with a magenta background using ANSI codes 
     * (increases real length)
     * @param str string to be converted
     * @return magenta background String (ANSI codes appended)
     */
    public static String magentaB(String str) {
        return "\033[45m" + str + "\033[0m";
    }

    /**
     * Returns the given String with a cyan background using ANSI codes 
     * (increases real length)
     * @param str string to be converted
     * @return cyan background String (ANSI codes appended)
     */
    public static String cyanB(String str) {
        return "\033[46m" + str + "\033[0m";
    }

    // number formatting
    /**
     * Rounds the number to two decimal places
     * @param value number to be rounded
     * @return double of rounded number
     */
    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Rounds the number to two decimal places using String.format()
     * @param value number to be rounded
     * @return the number as a formatted string
     */
    public static String sround(double value) {
        return String.format("%.2f", value);
    }

    /**
     * Returns a properly formatting String representing a given dollar value
     * Formats with the respective letter for millions, billions, and trillions
     * @param value number to be formatted
     * @return formatted String represented the dollar amount
     */
    public static String dollar(double value) {
        // Determine the abbreviation based on the value's size
        if (Math.abs(value) >= 1_000_000_000_000.0) {  // Trillions
            return String.format("%s$%.2f T", value < 0 ? "-" : "", Math.abs(value) / 1_000_000_000_000.0);
        } else if (Math.abs(value) >= 1_000_000_000.0) {  // Billions
            return String.format("%s$%.2f B", value < 0 ? "-" : "", Math.abs(value) / 1_000_000_000.0);
        } else if (Math.abs(value) >= 1_000_000.0) {  // Millions
            return String.format("%s$%.2f M", value < 0 ? "-" : "", Math.abs(value) / 1_000_000.0);
        } else {  // Less than a million, add commas
            return String.format("%s$%,.2f", value < 0 ? "-" : "", Math.abs(value));
        }
    }

    // calculations
    /**
     * Percent change calculation
     * @param previous previous value
     * @param current current value
     * @return the percent difference of the two values
     */
    public static double change(double previous, double current) {
        return ((current - previous) / previous) * 100;
    }

    /**
     * Pauses gameplay for a given amount of time
     * @param millis amount of time to wait
     */
    public static void pause(int millis) {
        pause(null, millis);
    }

    /**
     * Pauses gameplay until a new line is entered
     * @param scanner user input scanner
     */
    public static void pause(Scanner scanner) {
        pause(scanner, 0);
    }
    
    /**
     * Pauses gameplay for a given amount of time and then waits for a new line
     * to be entered
     * @param scanner user input scanner
     * @param millis amount of time to wait
     */
    public static void pause(Scanner scanner, int millis) {
        // so I don't have to type the try catch block ;)
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
        if (scanner != null) {
            // display message
            System.out.print(italic("Press Enter to Continue..."));
            scanner.nextLine();
        }
    }

    // DEBUGGING
    /**
     * Outputs a message to the universal debug log file "err.txt"
     * @param input message to be outputed
     */
    public static void DB_LOG(String input) {
        String logPath = System.getenv("APPDATA") + "\\Terminal Trader\\err.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(logPath, true))) {
            writer.println(input);
        } catch (IOException e) {
            System.err.println("IO Error: Unable to write to " + logPath);
            e.printStackTrace(System.err);
        }
    }
}