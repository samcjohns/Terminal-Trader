package tetrad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

import static tetrad.Mutil.DB_LOG;
import static tetrad.Mutil.MENU_WIDTH;
import static tetrad.Mutil.blue;
import static tetrad.Mutil.blueB;
import static tetrad.Mutil.bold;
import static tetrad.Mutil.center;
import static tetrad.Mutil.clearLine;
import static tetrad.Mutil.clearScreen;
import static tetrad.Mutil.cyan;
import static tetrad.Mutil.cyanB;
import static tetrad.Mutil.dollar;
import static tetrad.Mutil.green;
import static tetrad.Mutil.greenB;
import static tetrad.Mutil.italic;
import static tetrad.Mutil.magenta;
import static tetrad.Mutil.magentaB;
import static tetrad.Mutil.numColor;
import static tetrad.Mutil.pause;
import static tetrad.Mutil.printTaxMan;
import static tetrad.Mutil.red;
import static tetrad.Mutil.redB;
import static tetrad.Mutil.round;
import static tetrad.Mutil.yellow;
import static tetrad.Mutil.yellowB;

/**
 * Top-level class for game functionality.
 * 
 * @author Samuel Johns
 * Created: November 15, 2024
 * 
 * Description: A child class of Game that handles terminal output and user 
 *              input from the terminal. It is entirely responsible for the
 *              gameplay control flow and user input is not handled by any
 *              other class. It has a Market reference which holds all the 
 *              stocks, a News reference for events that occur ingame, and a
 *              User reference to keep track of important information for the
 *              current user.
 * 
 * @see User
 * @see Market
 * @see News
 */

public class Game {
    User usr;
    Market mkt;
    News news;

    static int headerSetting = -1; // color setting for the header
    static boolean ARCADE_MODE = false; // activates old stock behavior

    public Game() {
        news = new News();
        mkt = new Market(this);
        usr = new User(this);
    }

    /**
     * Top-level initialization function; Functions as the main menu and
     * handles new game creation or loading from an existing file.
     * @param scanner user input scanner
     * @return false if the user selects to exit the program
     */
    public boolean startGame(Scanner scanner) {
        while(true) {
            showMainMenu();
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> {
                    showLoadGameMenu();
                    String username = scanner.nextLine();
                    try {
                        loadGame(username);
                        initAdvance();
                        return true; // loading successful, exit method
                    }
                    catch (InitException e) {
                        clearScreen();
                        printHeader();
                        System.out.println("");
                        System.out.println(redB(e.getMessage()));
                        System.out.println("User: '" + username + "' cannot be accessed...");
                        System.out.println(italic("Try making a new save if this is your first time! :)"));
                        System.out.println("");
                        pause(scanner);
                        // error, so repeat
                    }
                }
                case "2" -> {
                    showNewGameMenu();
                    createSaveFile(scanner.nextLine());
                    initAdvance();
                    return true; // move on
                }
                case "3" -> doExtras(scanner);
                case "4" -> {
                    return false; // exit program
                }
                default -> {
                    System.out.println("Invalid Selection");
                    pause(scanner);
                    // error, so repeat
                }
            }
        }
    }

    /**
     * Main gameplay function, handles all gameplay until the user chooses to
     * exit. Use this method after calling startGame(). 
     * @param scanner user input scanner
     */
    public void play(Scanner scanner) {
        while (true) {
            clearScreen();
            printHeader();
            showPlayMenu();
            System.out.print("---Select: ");
            String input = scanner.nextLine();
            switch (input) {
                case "1" -> portfolioMenu(scanner); // show portfolio
                case "2" -> marketMenu(scanner);    // show stock exchange
                case "3" -> {
                    // user stats
                    clearScreen();
                    printHeader();
                    usr.showStats();
                    pause(scanner);
                }
                case "4" -> {
                    // achievements
                    clearScreen();
                    printHeader();
                    usr.showAchievements();
                    pause(scanner);
                }
                case "5" -> {
                    clearScreen();
                    printHeader();
                    news.page();
                    pause(scanner);
                }
                case "6" -> {
                    saveGame();
                    return;
                }

                // cheat codes :)
                // enter command, then enter value
                case "adv" -> {
                    input = scanner.nextLine();
                    advance(Integer.parseInt(input));
                }
                case "give" -> {
                    input = scanner.nextLine();
                    usr.setCash(Integer.parseInt(input) + usr.getCash());
                }
                default -> {
                    // help case
                    System.out.println("Invalid Selection");
                    pause(scanner);
                }
            }
        }
    }

    /**
     * Exit method, used for cleanup.
     */
    public void endGame() {
        // doesn't do anything yet, used for cleanup
    }

    /**
     * Main time advancement method, used to simulate a single day passing
     * across all objects.
     */
    private void advance() {
        mkt.advance();
        usr.update();
        news.update();
    }

    /**
     * Main time advancement method, used to simulate a single day passing
     * across all objects.
     * @param times number of times to advance
     */
    private void advance(int times) {
        for (int i = 0; i < times; i++) {
            this.advance();
        }
    }
    // specific advance for init case (only affects market values)
    protected void initAdvance() {
        mkt.advance();
    }
    
    /**
     * Main save function, calls all other save functions for objects
     * across the game.
     */
    public void saveGame() {
        mkt.save();
        usr.save();
    }

    /**
     * Main load method, calls all other load methods for objects across the
     * game.
     * @param username username of the user save file to be loaded
     * @throws InitException throws if user save file is not present
     */
    public void loadGame(String username) throws InitException {
        mkt.load();
        usr.load(username, mkt);
    }

    /**
     * Creates new save file for user.
     */
    private void createSaveFile(String username) {
        // check dependencies
        try {
            mkt.load();
        }
        catch (InitException e) {
            createGen();
        }

        usr = new User(username, User.STARTING_CASH, this);
        usr.save();
    }

    /**
     * Portfolio menu method, handles all game behavior within the menu
     * @param scanner user input scanner
     */
    private void portfolioMenu(Scanner scanner) {
        while (true) {
            clearScreen();
            printHeader();
            usr.showPortfolio();

            // command tray
            System.out.println("-".repeat(MENU_WIDTH));
            System.out.println(cyan("Enter - Exit") + " | " 
            + cyan("'/' - Advance") + " | " 
            + cyan("'.' - Sell") + " | "
            + cyan("; - History"));
            System.out.println("-".repeat(MENU_WIDTH));

            String command = scanner.nextLine();
            switch (command) {
                case "" -> { return; }
                case "/" -> advance();
                case "." -> doSell(scanner);
                case ";" -> usr.getPortfolio().printTransactionLogs();
                default -> {
                    System.out.println("Invalid Input");
                    pause(1000);
                }
            }
        }
    }

    /**
     * Market menu method, handles all game behavior within the market menu
     * @param scanner user input scanner
     */
    private void marketMenu(Scanner scanner) {
        while (true) {
            clearScreen();
            printHeader();
            news.roll();
            mkt.print();

            // command tray
            System.out.println(cyan("Enter - Exit | '/' - Advance | ',' - View | '.' - Buy"));
            System.out.println("-".repeat(MENU_WIDTH));  
            
            String command = scanner.nextLine();
            clearLine();
            switch (command) {
                case "" -> {
                    return;
                }
                case "/" -> advance();
                case "," -> stockView(scanner);
                case "." -> doBuy(scanner);
                default -> {
                    System.out.println(red("Invalid Command"));
                    pause(2000);
                }
            }
        }
    }

    /**
     * Shows information about a selected stock, handles all behavior within
     * the stock view menu
     * @param scanner user input scanner
     */
    private void stockView(Scanner scanner) {
        System.out.print("---Stock: ");
        String view = scanner.nextLine();
        while(!view.equals("")) {
            clearScreen();
            printHeader();
            Stock stock = mkt.getStock(Integer.parseInt(view) - 1);
            System.out.println(yellow(center(" " + stock.getName() + " ", MENU_WIDTH, "~")));
            System.out.println("-".repeat(MENU_WIDTH));
            stock.printHistory();
            System.out.println("-".repeat(MENU_WIDTH));

            System.out.println(
                blue(italic("Now: ")) + dollar(stock.getValue()) + " | " +
                red(italic("Min: ")) + dollar(stock.getMin()) + " | " +
                green(italic("Max: ")) + dollar(stock.getMax()) + " | " +
                yellow(italic("Recent: ")) + numColor(stock.getChange()) + " | " +
                yellow(italic("10-Day: ")) + numColor(stock.getChange(10)) + " | " +
                yellow(italic("100-Day: ")) + numColor(stock.getChange(100))
            );
            // command tray
            System.out.println("-".repeat(MENU_WIDTH));
            System.out.println(cyan("Enter - Exit") + " | " 
            + cyan("',' - Next") + " | " 
            + cyan("'.' - Buy") + " | "
            + cyan("'/' - Advance"));
            System.out.println("-".repeat(MENU_WIDTH));

            String command = scanner.nextLine();
            clearLine();
            switch (command) {
                case "" -> {
                    return;
                }
                case "," -> {
                    if (Integer.parseInt(view) < Market.NUM_STOCKS) {
                        view = "" + (Integer.parseInt(view) + 1);
                    }
                    else {
                        view = "" + 1;
                    }
                }
                case "." -> doBuy(scanner);
                case "/" -> {
                    advance();
                    pause(500);
                }
                default -> {
                    System.out.println(red("Invalid Command"));
                    pause(1000);
                }
            }
        }
    }

    /**
     * Extras menu method, directs control to other Extras menus
     * @param scanner user input scanner
     */
    private void doExtras(Scanner scanner) {
        clearScreen();
        printHeader();
        System.out.println(center("Extras", MENU_WIDTH, "~"));
        System.out.println(""); // spacing
        System.out.println("1. Settings");
        System.out.println("2. Developer Tools");
        System.out.println("3. Help");
        System.out.println("4. Mini Games!");
        System.out.println("5. Credits");
        System.out.println("6. Back");
        System.out.println(""); // spacing

        String input;
        while (true) {
            System.out.print("---Select: ");
            input = scanner.nextLine();
            clearLine();
            switch (input) {
                case "1" -> {
                    eSettings(scanner);
                    return;
                }
                case "2" -> {
                    eDevTools(scanner);
                    return;
                }
                case "3" -> {
                    eHelp(scanner);
                    return;
                }
                case "4" -> {
                    eMiniGames(scanner);
                    return;
                }
                case "5" -> {
                    eCredits(scanner);
                    return;
                }
                case "6" -> {
                    return;
                }
                default -> System.out.println("Invalid Input");
            }
        }
    }

    /**
     * Sell method, gets stock and amount to be sold and makes sale. Handles
     * errors appropriately and returns when user is finished.
     * @param scanner user input scanner
     */
    private void doSell(Scanner scanner) {
        while (true) {
            try {
                System.out.println("(0 to Exit)");
                System.out.print("--Stock: ");
                int selection = Integer.parseInt(scanner.nextLine());
                if (selection == 0) {
                    return; // exit case
                }
                clearLine();
                Stock stock = usr.getPortfolio().stockAt(selection - 1);

                System.out.print("---Amount: ");
                int amount = Integer.parseInt(scanner.nextLine());
                if (amount == 0) {
                    return; // exit case
                }
                clearLine();

                System.out.println(yellow(usr.sell(stock, amount)));
                pause(2000);
                break;
            } 
            catch (InvalidSelectionException e) {
                System.out.println(e.getMessage());
                pause(1000); // wait one second
                clearLine(2); // clear error message
            }
        }
    }

    /**
     * Buy method, gets stock and amount to be bought and makes sale. Handles
     * errors appropriately and returns when user is finished.
     * @param scanner user input scanner
     */
    private void doBuy(Scanner scanner) {
        while(true) {
            double cash = usr.getCash();
            try {
                System.out.print("---Stock: ");
                int selection = Integer.parseInt(scanner.nextLine());
                if (selection == 0) {
                    return; // exit case
                }
                clearLine();

                // show cash and max buy amount
                Stock stock = mkt.getStock(selection - 1);
                int maxAmount = (int) (cash / stock.getValue());
                System.out.println(yellow(bold("Total Cash: ")) + dollar(cash) + " | " 
                + yellow(bold("Max Amount: ")) + maxAmount);
                System.out.println("-".repeat(MENU_WIDTH));

                System.out.print("---Amount: ");
                int amount = Integer.parseInt(scanner.nextLine());
                if (amount == 0) {
                    return; // exit case
                }
                clearLine();

                System.out.println(yellow(usr.buy(stock, amount)));
                pause(2000);
                break;
            } 
            catch (NoSuchElementException | NumberFormatException e) {
                System.out.println("Invalid Input, please try again.");
                pause(scanner);
            }
            catch (InvalidSelectionException e)  {
                System.out.println(e.getMessage());
                pause(scanner);
            }
        }
    }

    // menu functions
    /**
     * Prints main menu
     * Height: 36 lines
     */
    private void showMainMenu() {
        clearScreen();
        printHeader();

        // main menu options
        System.out.println(center("Welcome to the show!", MENU_WIDTH));
        System.out.println(italic(center("Version 1.0", MENU_WIDTH)));

        printMenuArt(0);

        // command tray
        System.out.println("-".repeat(MENU_WIDTH));
        System.out.println(cyan("1. Load Game | 2. New Game | 3. Extras | 4. Exit"));
        System.out.println("-".repeat(MENU_WIDTH));
        System.out.print("Please make a selection: ");
    }

    /**
     * Prints play menu
     * Height: 36 lines
     */
    private void showPlayMenu() {
        clearScreen();
        printHeader();
        news.roll();
        System.out.println("-".repeat(MENU_WIDTH));

        // compressed menu art
        printMenuArt(2);

        // user quick stats
        System.out.println("-".repeat(MENU_WIDTH));
        System.out.print(red("Day: " + usr.getAdvances()) + " | ");
        System.out.print(yellow("Cash: " + dollar(usr.getCash())) + " | ");
        System.out.print(yellow("Net Worth: " + dollar(usr.getNet())) + " | ");
        System.out.print(green("Trader Score: " + usr.getScore()) + " | ");
        System.out.println(blue("Recent Performance: ") + numColor(usr.getPortfolio().getChange()));

        // command tray
        System.out.println("-".repeat(MENU_WIDTH));
        System.out.println(cyan("1. Your Portfolio | 2. Stock Exchange | 3. Your Stats | 4. Achievements | 5. News Feed | 6. Save and Exit"));
        System.out.println("-".repeat(MENU_WIDTH));
    }

    /**
     * Prints new game menu
     */
    private void showNewGameMenu() {
        clearScreen();
        printHeader();
        System.out.println();
        System.out.println(italic(center("<----------New Game---------->", MENU_WIDTH)));

        // print main menu, but clear command tray
        printMenuArt(0);
        System.out.println("-".repeat(MENU_WIDTH));
        System.out.println("New Account Name: ");
        System.out.println("-".repeat(MENU_WIDTH));

        // moves the cursor up and forward for typing
        Mutil.cursorUp(2);
        Mutil.cursorRight("New Account Name: ".length());
    }

    /**
     * Prints load game menu
     */
    private void showLoadGameMenu() {
        clearScreen();
        printHeader();
        System.out.println();
        System.out.println(italic(center("<----------Load Game---------->", MENU_WIDTH)));

        // funny bit for now
        Random rand = new Random();
        if (rand.nextInt(100) == 1) {
            System.out.println("");
            printTaxMan();
        }
        else {
            // print main menu, but clear command tray
            printMenuArt(0);
            System.out.println("-".repeat(MENU_WIDTH));
            System.out.println("Account Name: ");
            System.out.println("-".repeat(MENU_WIDTH));

            // moves the cursor up and forward for typing
            Mutil.cursorUp(2);
            Mutil.cursorRight("Account Name: ".length());
        }
    }

    /**
     * Prints the main menu ASCII art
     * @param less The amount that the art will be shortened to fit in the
     * current screen
     */
    private void printMenuArt(int less) {
        // main menu art
        String filePath;
        if (Main.NDEV) {
            filePath = "C:\\Program Files\\Terminal Trader\\assets\\city-skyline-120.txt";
        }
        else {
            filePath = "assets\\city-skyline-120.txt";
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            // truncate for less
            for (int i = 0; i < 27; i++) {
                if (less != 0) {
                    reader.readLine(); // skip current line
                    less--;
                }
            }

            while ((line = reader.readLine()) != null) {
                System.out.println(cyan(center(line, MENU_WIDTH)));
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }

    // extras
    /**
     * Settings menu method, handles everything in Settings and returns when
     * the user is finished.
     * @param scanner user input scanner
     */
    private void eSettings(Scanner scanner) {
        while (true) { 
            clearScreen();
            printHeader();
            System.out.println(center("Settings", MENU_WIDTH, "="));
            System.out.println(italic(center("Changes only affect current session", MENU_WIDTH)));
            System.out.println(""); // spacing
            System.out.println(center("1. Arcade Stock Behavior", MENU_WIDTH));
            System.out.println(center("2. Change Header Color", MENU_WIDTH));
            System.out.println(center("Enter to Exit", MENU_WIDTH));
            System.out.println("");

            System.out.print("---Select: ");
            String input = scanner.nextLine();
            clearLine();
            
            if (input.equals("1")) {
                System.out.print("Activate Arcade Stock Behavior [Y/N]: ");
                input = scanner.nextLine();
                clearLine();

                if (input.toUpperCase().equals("Y")) {
                    Game.ARCADE_MODE = true;
                    System.out.println("Arcade Mode Activated.");
                    pause(scanner);
                }
                else if (input.toUpperCase().equals("N")) {
                    Game.ARCADE_MODE = false;
                    System.out.println("Arcade Mode Deactivated.");
                    pause(scanner);
                }
                else {
                    System.out.println("Invalid Input");
                    pause(scanner);
                }
            }
            else if (input.equals("2")) {
                System.out.print("Enter Color Code (0-11): ");
                input = scanner.nextLine();
                clearLine();
                try {
                    int numChoice = Integer.parseInt(input);
                    if (numChoice >= -1 && numChoice <= 11) {
                        Game.headerSetting = numChoice;
                    }
                    else {
                        throw new NumberFormatException();
                    }
                } 
                catch (NumberFormatException e) {
                    System.out.println("Invalid Input");
                    pause(scanner);
                }
            }
            else {
                return;
            }
        }
    }

    /**
     * Helps menu method, handles everything within help and returns when user
     * is finished.
     * @param scanner user input scanner
     */
    private void eHelp(Scanner scanner) {
        clearScreen();
        printHeader();
        System.out.println(center(" Help ", MENU_WIDTH, "#"));
        System.out.println("");
        System.out.println(italic(center("Terminal Trader is a text-based, terminal stock trading simulator. Stock purchases", MENU_WIDTH)));
        System.out.println(italic(center("can be made in the 'Stock Exchange' and sales can be made in 'Your Portfolio'.", MENU_WIDTH)));
        System.out.println("");
        System.out.println(italic(center("Events that occur for stocks will show at the headline of each page and more", MENU_WIDTH)));
        System.out.println(italic(center("information about each event can be found in the 'News Feed' section.", MENU_WIDTH)));
        System.out.println("");
        System.out.println(italic(center("Achievements are calculated after purchases, sales, and advances.", MENU_WIDTH)));
        System.out.println("");
        System.out.println(italic(center("Terminal Trader is currently in beta and features may be added or removed after each update. ", MENU_WIDTH)));
        System.out.println(italic(center("The next update will most likely feature achievement rewards and a live leaderboard!", MENU_WIDTH)));
        System.out.println("");
        System.out.println(green(italic(center("Any questions or suggestions can be directed sent to:", MENU_WIDTH))));
        System.out.println(green(italic(center("Samuel Johns at samueljohns@cedarville.edu ", MENU_WIDTH))));
        System.out.println("");
        System.out.println(""); // spacing
        pause(scanner);
    }

    /**
     * Minigames menu method, handles everything for mini games and returns 
     * when the user is finished.
     * @param scanner user input scanner
     */
    private void eMiniGames(Scanner scanner) {
        clearScreen();
        printHeader();
        System.out.println(center(" Mini Games ", MENU_WIDTH, "!"));
        System.out.println("");
        System.out.println("1. Coin Toss");
        System.out.println("");
        System.out.println(""); // spacing
        
        String choice = scanner.nextLine();
        switch (choice) {
            case "1" -> {
                MiniGames.coinToss(scanner);
                pause(scanner);
            }
        }
    }

    /**
     * Prints credits and waits for user to press enter.
     * @param scanner user input scanner
     */
    private void eCredits(Scanner scanner) {
        clearScreen();
        printHeader();
        System.out.println(center(" Credits ", MENU_WIDTH, "#"));
        System.out.println("");
        System.out.println(italic(center("'Terminal Trader' was designed, implemented, and produced by Samuel Johns, 2024", MENU_WIDTH)));
        System.out.println("");
        System.out.println(italic(center("Playtesters:", MENU_WIDTH)));
        System.out.println(blue(italic(center("Logan J Burley", MENU_WIDTH))));
        System.out.println(red(italic(center("Colin Burley", MENU_WIDTH))));
        System.out.println(green(italic(center("Jaden Butler", MENU_WIDTH))));
        System.out.println("");
        System.out.println(""); // spacing
        pause(scanner);
    }

    /**
     * Dev Tools menu method, handles everything for Dev Tools and returns when
     * the user is finished.
     * @param scanner user input scanner
     */
    private void eDevTools(Scanner scanner) {
        while (true) {
            clearScreen();
            printHeader();

            System.out.println(center(" Developer Tools ", MENU_WIDTH, "#"));
            System.out.println(""); // spacing
            System.out.println("0. Back");
            System.out.println(redB("1. Repair Asset Files"));
            System.out.println(redB("2. Stock Walk"));
            System.out.println(""); // spacing
            System.out.print("---Select: ");

            String input = "";
            
            input = scanner.nextLine();
            clearLine();
            switch (input) {
                case "0" -> {
                    return;
                }
                case "1" -> {
                    createGen();
                    System.out.println("Regenerated Assets");
                    pause(scanner);
                }
                case "2" -> {
                    while (true) {
                        System.out.print("---Stock: ");
                        try {
                            // get selection
                            int stockID = Integer.parseInt(scanner.nextLine());
                            clearLine();
                            System.out.print("---Times: ");
                            input = scanner.nextLine();

                            // load mkt assets
                            try {
                                mkt.load();
                            } catch (InitException e) {
                                createGen();
                            }

                            // call function
                            stockWalk(stockID, Integer.parseInt(input));
                            pause(scanner);
                        } 
                        catch (NumberFormatException e) {
                            System.out.println("Invalid Input");
                            pause(scanner);
                        }
                    }
                }
                default -> System.out.println("Invalid Input");
            }   
        }
    }

    // static menu functions
    /**
     * Prints the game header
     */
    public static void printHeader() {
        // title card
        String title = "Terminal Trader";
        String line1 = "-".repeat(MENU_WIDTH);
        String line2 = "~ ".repeat((MENU_WIDTH - title.length())/4) + title + " ~".repeat((MENU_WIDTH - title.length())/4) + " ";
        String line3 = "-".repeat(MENU_WIDTH);

        switch (headerSetting) {
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

    // dev tools
    /**
     * Creates instances of each stock and then saves them to create the
     * default stock save files.
     */
    private void createGen() {
        // builds save files for stocks and market
        mkt = new Market(this);

        // prep market
        mkt.setTrend(1); // start market a tad promising
        mkt.setNUM_STOCKS(15); 

        try {
            Stock s0 = new Stock(0, "Burley Buns", 18.24, 0.7, 2);
            Stock s1 = new Stock(1, "Logan Logs", 100.00, 1, 1);
            Stock s2 = new Stock(2, "Shorehaven", 17000.00, 0.5, 1);
            Stock s3 = new Stock(3, "GreenCard", 42.00, 0.9, 3);
            Stock s4 = new Stock(4, "MurphCo", 64.00, 1.1, 2);
            Stock s5 = new Stock(5, "Orion", 10.00, 1.4, 1);
            Stock s6 = new Stock(6, "Rinnova", 1200, 1.2, 1);
            Stock s7 = new Stock(7, "Beans & Cream", 56.00, 1.3, 1);
            Stock s8 = new Stock(8, "Mom & Dad's", 789.00, 0.6, 1);
            Stock s9 = new Stock(9, "The Rip", 420.00, 2.7, 3);

            // new!
            Stock s10 = new Stock(10, "Sam's Johns", 67.00, 1, 1);
            Stock s11 = new Stock(11, "Jenna Gyms", 140.00, 2, 2);
            Stock s12 = new Stock(12, "Rockford Mine", 13000.00, 1, 3);
            Stock s13 = new Stock(13, "Colin Call-Center", 240.00, 2.9, 3);
            Stock s14 = new Stock(14, "Dave's Diner", 150.00, 1, 1);

            s0.save();
            s1.save();
            s2.save();
            s3.save();
            s4.save();
            s5.save();
            s6.save();
            s7.save();
            s8.save();
            s9.save();
            s10.save();
            s11.save();
            s12.save();
            s13.save();
            s14.save();
        } 
        catch (InitException e) {
            DB_LOG("Exception In: createGen(), THIS: " + e.getMessage());
        }

        mkt.save();

        try { 
            mkt.load(); // mkt will now look for the stocks created above.
        } 
        catch (InitException e) { 
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }

        // advance time to generate histories so early game graphs aren't empty
        advance(120);

        // save files
        mkt.save();
        news.clear(); // clear acv caused by advancing
    }

    /**
     * Prints a window with stock performance information and then advances
     * slowly, a given number of times, to monitor stock performance for a 
     * given algorithm. Used in initial testing, kept in the game for fun.
     * @param stockID the id of the stock to be monitored.
     * @param times the number of times to advance.
     */
    private void stockWalk(int stockID, int times) {
        Stock stock = mkt.getStock(stockID);
        for (int i = 0; i < times; i++) {
            clearScreen();
            System.out.print(stock.getName() + " | ");
            System.out.print("At " + dollar(stock.getValue()) + " | ");
            System.out.print("Target Value: " + dollar(stock.getTargetVal()) + " | ");
            System.out.print("Trend: " + round(stock.getTrend()) + " | ");
            System.out.println("Vol: " + round(stock.getVol()));
            System.out.println("-".repeat(MENU_WIDTH));
            stock.printHistory();
            System.out.println("-".repeat(MENU_WIDTH));
            pause(100); // pause 1/10 of second
            initAdvance();
        }
    }
}