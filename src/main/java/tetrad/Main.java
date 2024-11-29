package tetrad;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static tetrad.Mutil.pause;

/**
 * Top-level functionality class
 * @author Samuel Johns
 * Created: November 15, 2024
 * 
 * @see Game
 */

public class Main {    
    static boolean NDEV = false; // false when testing in VSCode
    static boolean INIT = false; // true for first start only
    
    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                NDEV = args[0].equals("-NDEV");
                if (args.length > 1) {
                    INIT = args[1].equals("-INIT");
                }
            }

            boolean exit = false;

            // DEBUG: edit me
            boolean DEBUG = false;
            int DEBUG_MODE = 8;

            Scanner scanner = getSource(DEBUG, DEBUG_MODE);
            SoundPlayer theme = new SoundPlayer("tetrad-theme");
            
            try {
                while (true) {
                    Game game = new Game();
                    theme.play();
                    // exits if user selects exit
                    if (!game.startGame(scanner)) {
                        theme.close();
                        scanner.close();
                        return;
                    }
                    game.play(scanner);
                    game.endGame();
                    theme.stop();
                }
            }
            catch (NoSuchElementException e) {
                // DEBUG
                theme.close();
                scanner.close();
                while(true) {} //suspend prog
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            pause(10000);
        }
    }

    private static Scanner getSource(boolean DEBUG, int MODE) {
        if (DEBUG) {
            // determine test input file
            String fileName;
            switch (MODE) {
                case 1 -> fileName = "view_stock_test";
                case 2 -> fileName = "view_portfolio";
                case 3 -> fileName = "view_market";
                case 4 -> fileName = "repair_dependencies";
                case 5 -> fileName = "view_new_market";
                case 6 -> fileName = "new_save_plus";
                case 7 -> fileName = "load_new_save";
                case 8 -> fileName = "stock_walk";

                default -> fileName = "exit";
            }

            // open scanner and return
            try {
                fileName = "DEBUG/" + fileName + ".in";
                return new Scanner(new File(fileName));   
            }
            catch (FileNotFoundException e) {
                System.out.println("File Not Found");
                return null;
            }
        }
        else {
            return new Scanner(System.in);
        }
    }

    public static void systemCheck(String[] args) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            System.out.println("Running on Windows");
        } else if (os.contains("mac")) {
            System.out.println("Running on macOS");
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            System.out.println("Running on Linux/Unix");
        } else {
            System.out.println("Unknown operating system");
        }

        String term = System.getenv("TERM");
        if (term != null) {
            System.out.println("Terminal detected: " + term);
        } else {
            System.out.println("No terminal detected");
        }

        boolean isHeadless = java.awt.GraphicsEnvironment.isHeadless();
        if (isHeadless) {
            System.out.println("Running in a headless environment (likely a console).");
        } else {
            System.out.println("Running in a graphical environment.");
        }

        String term1 = System.getenv("TERM");
        String wtSession = System.getenv("WT_SESSION"); // Windows Terminal
        String conemu = System.getenv("ConEmu");        // ConEmu/Cmder
        String colorterm = System.getenv("COLORTERM");  // Common in modern terminals

        if (wtSession != null) {
            System.out.println("Running in Windows Terminal.");
        } else if (conemu != null) {
            System.out.println("Running in ConEmu or Cmder.");
        } else if (colorterm != null) {
            System.out.println("Running in a color-capable terminal: " + colorterm);
        } else if (term1 != null) {
            System.out.println("Terminal detected: " + term1);
        } else {
            System.out.println("No terminal environment variables detected.");
        }
    }
}
