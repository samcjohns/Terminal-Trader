package tetrad;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import static tetrad.Mutil.MENU_WIDTH;
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
    static boolean   INIT = false;

    private static final int MIN_TERMINAL_WIDTH = 120;
    private static final int MIN_TERMINAL_HEIGHT = 36;

    private static final String ENV_DATA_ROOT = "TT_DATA_ROOT";
    private static final String ENV_APP_ROOT = "TT_APP_ROOT";
    private static final String ENV_IDENTITY = "TT_IDENTITY";
    private static final String ENV_SSH_KEY_FP = "TT_SSH_KEY_FP";
    private static final String ENV_SSH_PUBLIC_KEY = "TT_SSH_PUBLIC_KEY";

    private static Path appRoot;
    private static Path dataRoot;
    private static Path playerRoot;
    private static String resolvedIdentity;
    
    public static void main(String[] args) {
        Game game = null; // main game object
        try {
            initRuntime();

            Scanner scanner = new Scanner(System.in);
            startup(scanner); // init dialogue

            game = new Game(scanner);
            if (!game.startGame()) {
                scanner.close();
                return;
            }

            warnIfTerminalTooSmall(scanner);
            game.play();
            game.endGame();
            scanner.close();
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

    static String getIdentity() {
        return resolvedIdentity;
    }

    /**
     * Used to get the soure or destination of save file depending of
     * environment. Takes the directory as a string and to determine 
     * destination.
     * @param dir save directory
     * @return file path of the save file
     */
    static String getSource(String dir) {
        return switch (dir) {
            case "saves" -> sourcePath(playerRoot.resolve("saves"));
            case "gen" -> sourcePath(playerRoot.resolve("gen"));
            case "logs" -> sourcePath(playerRoot.resolve("logs"));
            case "assets" -> sourcePath(appRoot.resolve("assets"));
            case "wav" -> sourcePath(appRoot.resolve("wav"));
            default -> sourcePath(appRoot.resolve(dir));
        };
    }

    private static String sourcePath(Path path) {
        return path.toString() + File.separator;
    }

    private static void initRuntime() throws IOException {
        appRoot = resolveRoot(System.getenv(ENV_APP_ROOT), ".");
        dataRoot = resolveRoot(System.getenv(ENV_DATA_ROOT), appRoot.resolve("data").toString());

        resolvedIdentity = resolveIdentity();
        playerRoot = dataRoot.resolve("players").resolve(resolvedIdentity);

        Files.createDirectories(playerRoot.resolve("saves"));
        Files.createDirectories(playerRoot.resolve("gen"));
        Files.createDirectories(playerRoot.resolve("logs"));
    }

    private static Path resolveRoot(String value, String fallback) {
        String source = (value == null || value.isBlank()) ? fallback : value;
        return Paths.get(source).toAbsolutePath().normalize();
    }

    private static String resolveIdentity() {
        String explicit = sanitizeIdentity(System.getenv(ENV_IDENTITY));
        if (!explicit.equals("guest")) {
            return explicit;
        }

        String fp = sanitizeIdentity(System.getenv(ENV_SSH_KEY_FP));
        if (!fp.equals("guest")) {
            return "fp_" + fp;
        }

        String pub = System.getenv(ENV_SSH_PUBLIC_KEY);
        if (pub != null && !pub.isBlank()) {
            return "key_" + hash(pub);
        }

        return "guest";
    }

    private static String sanitizeIdentity(String raw) {
        if (raw == null || raw.isBlank()) {
            return "guest";
        }

        String sanitized = raw.trim().toLowerCase().replaceAll("[^a-z0-9._-]", "_");
        sanitized = sanitized.replaceAll("_+", "_");
        sanitized = sanitized.replaceAll("^[_-]+|[_-]+$", "");
        if (sanitized.isBlank()) {
            return "guest";
        }

        return sanitized.length() > 64 ? sanitized.substring(0, 64) : sanitized;
    }

    private static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.substring(0, 16);
        }
        catch (NoSuchAlgorithmException e) {
            return "guest";
        }
    }

    private static void warnIfTerminalTooSmall(Scanner scanner) {
        TerminalSize size = detectTerminalSize();
        if (!size.isKnown()) {
            return;
        }

        if (size.width >= MIN_TERMINAL_WIDTH && size.height >= MIN_TERMINAL_HEIGHT) {
            return;
        }

        clearScreen();
        System.out.println("-".repeat(MENU_WIDTH));
        System.out.println(red(center("TERMINAL SIZE WARNING", MENU_WIDTH)));
        System.out.println("-".repeat(MENU_WIDTH));
        System.out.println("\nCurrent terminal size: " + size.width + " x " + size.height + " (width x height)");
        System.out.println("Recommended minimum: " + MIN_TERMINAL_WIDTH + " x " + MIN_TERMINAL_HEIGHT);
        System.out.println("Please make your terminal larger for the best gameplay experience.\n");
        pause(scanner);
        clearLine();
    }

    private static TerminalSize detectTerminalSize() {
        int width = parsePositiveInt(System.getenv("COLUMNS"));
        int height = parsePositiveInt(System.getenv("LINES"));
        if (width > 0 && height > 0) {
            return new TerminalSize(width, height);
        }

        try {
            Process process = new ProcessBuilder("sh", "-c", "stty size < /dev/tty").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                int exitCode = process.waitFor();
                if (exitCode == 0 && line != null) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length == 2) {
                        int rows = parsePositiveInt(parts[0]);
                        int cols = parsePositiveInt(parts[1]);
                        if (cols > 0 && rows > 0) {
                            return new TerminalSize(cols, rows);
                        }
                    }
                }
            }
        }
        catch (Exception ignored) {
            // Best-effort detection only. If unavailable, skip warning.
        }

        return new TerminalSize(-1, -1);
    }

    private static int parsePositiveInt(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }

        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : -1;
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }

    private static final class TerminalSize {
        final int width;
        final int height;

        TerminalSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        boolean isKnown() {
            return width > 0 && height > 0;
        }
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

        System.out.println("Session identity: " + getIdentity());
        System.out.println("Data root: " + dataRoot);
        pause(250); // pause for effect ;)
        System.out.println("Starting now...");
        pause(500);
    }
}