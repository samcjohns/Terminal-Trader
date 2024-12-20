package tetrad;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import static tetrad.ScreenBuilder.MENU_WIDTH;
import static tetrad.Mutil.center;
import static tetrad.Mutil.clearLine;
import static tetrad.Mutil.clearScreen;
import static tetrad.Mutil.pause;
import static tetrad.Mutil.red;

/**
 * Top-level functionality class
 * @author Samuel Johns
 * Created: November 15, 2024
 * 
 * @see Game
 */

public class Main {    
    static String version = "1.1.1"; // current game version
    static boolean   PROD = false;    // false when testing in VSCode
    static boolean   INIT = false;
    
    public static void main(String[] args) {
        Game game = null; // main game object
        try {
            if (args.length > 0) {
                PROD = args[0].equals("-PROD");
            }

            Scanner scanner = new Scanner(System.in);
            startup(scanner); // init dialogue
    
            while (true) {
                game = new Game(scanner);
                // exits if user selects exit
                if (!game.startGame()) {
                    scanner.close();
                    return;
                }
                game.play();
                game.endGame();
            }
        }
        catch (Exception e) {
            // fatal error report
            // save user's progress
            if (game != null) {
                game.saveGame();
            }
            else {
                // fatal error in Main, exit
                return;
            }
            
            // error message screen
            clearScreen();
            System.out.println("-".repeat(MENU_WIDTH)); 
            System.out.println(red(center("Fatal Error Report", MENU_WIDTH)));
            System.out.println("-".repeat(MENU_WIDTH));
            System.out.println(red("""
                    Uh oh! A fatal error has occured. Don't worry, your progress has been saved.

                    If you would like to contribute to the developement of Terminal Trader, please feel free to screenshot this error 
                    message. You can submit it as an issue in the public Github Repository or send it directly to Samuel Johns.

                    Github: www.github.com/samcjohns/Terminal-Trader
                    Email: samueljohns@cedarville.edu

                    This error dialogue will automatically close in 60 seconds. """));
            System.out.println("-".repeat(MENU_WIDTH));
            System.out.println("Message: " + e.getMessage());
            System.out.println("Version: " + version);
            System.out.println("Advances: " + game.usr.getAdvances());
            System.out.println("Cash: " + game.usr.getCash());
            System.out.println("OS: " + System.getProperty("os.name"));
            System.out.println("Max Memory: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " MB");
            System.out.println("Total Memory: " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + " MB");
            System.out.println("Free Memory: " + Runtime.getRuntime().freeMemory() / 1024 / 1024 + " MB");

            System.out.println("\nStack Trace:");
            e.printStackTrace();
            pause(60000);
        }
    }

    /**
     * Used to get the soure or destination of save file depending of
     * environment. Takes the directory as a string and to determine 
     * destination.
     * @param dir save directory
     * @return file path of the save file
     */
    static String getSource(String dir) {
        String os = System.getProperty("os.name").toLowerCase();
        
        // Windows
        if (os.contains("win")) {
            if (dir.equals("saves") || dir.equals("gen")) {
                // Production environment
                if (PROD) {
                    return System.getenv("APPDATA") + "\\Terminal Trader\\" + dir + "\\";
                } 
                // Development (VSCode)
                else {
                    return dir + "/";
                }
            } else if (dir.equals("assets")) {
                if (PROD) {
                    return "C:\\Program Files\\Terminal Trader\\" + dir + "\\";
                } else {
                    return dir + "/";
                }
            }
        }
    
        // macOS
        else if (os.contains("mac")) {
            if (dir.equals("saves") || dir.equals("gen")) {
                // Production environment
                if (PROD) {
                    return System.getProperty("user.home") + "/Library/Application Support/Terminal Trader/" + dir + "/";
                } 
                // Development (VSCode)
                else {
                    return dir + "/";
                }
            } else if (dir.equals("assets")) {
                if (PROD) {
                    return "/Applications/Terminal Trader/" + dir + "/";
                } else {
                    return dir + "/";
                }
            }
        }
    
        // Linux
        else {
            if (dir.equals("saves") || dir.equals("gen")) {
                if (PROD) {
                    return System.getProperty("user.home") + "/.terminal-trader/" + dir + "/";
                } else {
                    return dir + "/";
                }
            } else if (dir.equals("assets")) {
                if (PROD) {
                    return "/usr/local/share/Terminal Trader/" + dir + "/";
                } else {
                    return dir + "/";
                }
            }
        }
    
        // Default to returning the directory itself if no match
        return dir + "/";
    }

    static void startup(Scanner scanner) {
        clearScreen();
        // checks if init file exists, if it does, this is first startup
        String filePath = getSource("gen") + "init";
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    INIT = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            INIT = false;
        }

        // configuration message
        if (INIT) {
            System.out.println("-".repeat(MENU_WIDTH));
            System.out.println(red(center("ATTENTION", MENU_WIDTH)));
            System.out.println("-".repeat(MENU_WIDTH));
            System.out.println("\nIt is strongly recommended that Terminal Trader is played in a terminal 120 characters wide by 36 lines tall.");
            System.out.println("Please set the correct dimensions and restart the game. You may also play in fullscreen and ignore this message.\n");
            pause(scanner);
            clearLine();
        }

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

        pause (250); // pause for effect ;)

        String term1 = System.getenv("TERM");
        String wtSession = System.getenv("WT_SESSION"); // Windows Terminal
        String conemu = System.getenv("ConEmu");        // ConEmu/Cmder
        String colorterm = System.getenv("COLORTERM");  // Common in modern terminals

        pause(250);
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
        System.out.println("Starting now...");
        pause(500);
    }
}