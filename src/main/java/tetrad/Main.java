package tetrad;

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
    static boolean PROD = false; // false when testing in VSCode
    
    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                PROD = args[0].equals("-PROD");
            }

            Scanner scanner = new Scanner(System.in);
    
            while (true) {
                Game game = new Game();
                // exits if user selects exit
                if (!game.startGame(scanner)) {
                    scanner.close();
                    return;
                }
                game.play(scanner);
                game.endGame();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            pause(10000);
        }
    }

    /**
     * Used to get the soure or destination of save file depending of
     * environment. Takes the directory as a string and to determine 
     * destination.
     * @param dir save directory
     * @return file path of the save file
     */
    public static String getSource(String dir) {
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
