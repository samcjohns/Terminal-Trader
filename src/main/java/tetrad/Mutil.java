package tetrad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Mutil {

    public static final int MENU_WIDTH = 100;

    // variables for printing history graph
    // MAY CHANGE LATER WITH A SETTINGS UPDATE
    public static final int HISTORY_LENGTH = 100;
    public static final int HISTORY_HEIGHT = 15;

    // formatting
    public static String center(String string, int width, String mark) {
         if (string.length() < width) {
            return (mark.repeat((width - string.length())/2) + string + mark.repeat((width - string.length())/2));
         } 
         else {
            return string; // string too big, just return the whole thing
         }
    }
    public static String center(String string, int width) {
        return center(string, width, " ");
    }
    public static String center(int num, int width) {
        return center("" + num, width, " ");
    }
    public static String center(double num, int width) {
        return center("" + num, width, " ");
    }
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

    public static String numColor(double value) {
        if (value >= 0) {
            return green(" " + value); // space for minus sign
        } 
        else {
            return red(value + "");
        }
    }
    public static String numColor(int value) {
        return numColor((double) value);
    }
    public static String numColor(String value) {
        try {
            double numericValue = Double.parseDouble(value);
            return numColor(numericValue);
        } catch (NumberFormatException e) {
            return "Invalid number format";
        }
    }

    // text styles
    public static String italic(String str) {
        return "\033[3m" + str + "\033[0m";
    }
    public static String bold(String str) {
        return "\033[1m" + str + "\033[0m";
    }

    // text colors
    public static String red(String str) {
        return "\033[31m" + str + "\033[0m";
    }
    public static String green(String str) {
        return "\033[32m" + str + "\033[0m";
    }
    public static String blue(String str) {
        return "\033[34m" + str + "\033[0m";
    }
    public static String yellow(String str) {
        return "\033[33m" + str + "\033[0m";
    }
    public static String magenta(String str) {
        return "\033[35m" + str + "\033[0m";
    }
    public static String cyan(String str) {
        return "\033[36m" + str + "\033[0m";
    }

    // text background colors
    public static String redB(String str) {
        return "\033[41m" + str + "\033[0m";
    }
    public static String greenB(String str) {
        return "\033[42m" + str + "\033[0m";
    }
    public static String blueB(String str) {
        return "\033[44m" + str + "\033[0m";
    }
    public static String yellowB(String str) {
        return "\033[43m" + str + "\033[0m";
    }
    public static String magentaB(String str) {
        return "\033[45m" + str + "\033[0m";
    }
    public static String cyanB(String str) {
        return "\033[46m" + str + "\033[0m";
    }

    // number formatting
    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
    public static String sround(double value) {
        return String.format("%.2f", value);
    }
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
    public static double change(double previous, double current) {
        return ((current - previous) / previous) * 100;
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J"); // clear screen
        System.out.println("\033[3J"); // clear scrollback buffer
        System.out.flush();
    }
    public static void clearLine() {
        System.out.print("\033[2K");  // Clears the second line
        System.out.print("\033[1F");  // Moves the cursor up to the first line
        System.out.print("\033[2K");  // Clears the first line
        System.out.print("\033[0G");  // Move cursor back to the beginning of the line
    }
    public static void printHeader() {
        // title card
        String title = "Terminal Trader";
        String line1 = "-".repeat(MENU_WIDTH);
        String line2 = "~ ".repeat((MENU_WIDTH - title.length())/4) + title + " ~".repeat((MENU_WIDTH - title.length())/4) + " ";
        String line3 = "-".repeat(MENU_WIDTH);

        switch (Game.headerSetting) {
            case 0 -> {
                line1 = blue(line1);
                line2 = blue(line2);
                line3 = blue(line3);
            }
            case 1 -> {
                line1 = green(line1);
                line2 = green(line2);
                line3 = green(line3);
            }
            case 2 -> {
                line1 = magenta(line1);
                line2 = magenta(line2);
                line3 = magenta(line3);
            }
            case 3 -> {
                line1 = cyan(line1);
                line2 = cyan(line2);
                line3 = cyan(line3);
            }
            case 4 -> {
                line1 = yellow(line1);
                line2 = yellow(line2);
                line3 = yellow(line3);
            }
            case 5 -> {
                line1 = yellowB(line1);
                line2 = yellowB(line2);
                line3 = yellowB(line3);
            }
            case 6 -> {
                line1 = greenB(line1);
                line2 = greenB(line2);
                line3 = greenB(line3);
            }
            case 7 -> {
                line1 = redB(line1);
                line2 = redB(line2);
                line3 = redB(line3);
            }
            case 8 -> {
                line1 = cyanB(line1);
                line2 = cyanB(line2);
                line3 = cyanB(line3);
            }
            case 9 -> {
                line1 = red(line1);
                line2 = red(line2);
                line3 = red(line3);
            }
            case 10 -> {
                line1 = magentaB(line1);
                line2 = magentaB(line2);
                line3 = magentaB(line3);
            }
            case 11 -> {
                line1 = blueB(line1);
                line2 = blueB(line2);
                line3 = blueB(line3);
            }
        }

        // print header
        System.out.println(line1);
        System.out.println(line2);
        System.out.println(line3);
    }
    public static void printTaxMan() {
        String filePath = "assets/taxman70.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(center(" | " + line + " | ", MENU_WIDTH));
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
    public static void pause(int millis) {
        pause(null, millis);
    }
    public static void pause(Scanner scanner) {
        pause(scanner, 0);
    }
    public static void pause(Scanner scanner, int millis) {
        // so I don't have to type the try catch block ;)
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
        if (scanner != null) {
            // display message
            System.out.println(italic("Press Enter to Continue..."));
            scanner.nextLine();
        }
    }

    // DEBUGGING
    public static void DB_LOG(String input) {
        String logPath = System.getenv("APPDATA") + "\\Terminal Trader\\err.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(logPath, true))) {
            writer.println(input);
        } catch (IOException e) {
            System.err.println("IO Error: Unable to write to " + logPath);
            e.printStackTrace(System.err);
        }
    }
    public static void DB_LOG(int input) {
        DB_LOG(String.valueOf(input));
    }
    public static void DB_LOG(double input) {
        DB_LOG(String.valueOf(input));
    }
}
