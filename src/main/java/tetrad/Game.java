package tetrad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

import static tetrad.Mutil.MENU_WIDTH;
import static tetrad.Mutil.blue;
import static tetrad.Mutil.center;
import static tetrad.Mutil.clearLine;
import static tetrad.Mutil.clearScreen;
import static tetrad.Mutil.cyan;
import static tetrad.Mutil.dollar;
import static tetrad.Mutil.green;
import static tetrad.Mutil.italic;
import static tetrad.Mutil.numColor;
import static tetrad.Mutil.pause;
import static tetrad.Mutil.printHeader;
import static tetrad.Mutil.printTaxMan;
import static tetrad.Mutil.red;
import static tetrad.Mutil.redB;
import static tetrad.Mutil.round;
import static tetrad.Mutil.yellow;
public class Game {
    User usr;
    Market mkt;
    News news;

    static int headerSetting = -1; // color setting for the header
    static boolean ARCADE_MODE = false; // activates old stock behavior

    public Game() {
        news = new News();
        mkt = new Market(news);
        usr = new User(news, this);
    }

    // public control functions
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
    public boolean doTurn(Scanner scanner) {
        showTurnMenu();
        System.out.print("---Select: ");
        String input = scanner.nextLine();
        switch (input) {
            case "1" -> {
                // show portfolio
                usr.showPortfolio(scanner);
            }
            case "2" -> {
                // show stock exchange
                clearScreen();
                printHeader();
                mkt.print(true);
                marketView(scanner);
            }
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
                return true;
            }

            // debug codes :)
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
        return false;
    }
    public void endGame() {
        // doesn't do anything yet, used for cleanup
    }
    
    // private gameplay functions
    private void marketView(Scanner scanner) {
        while (true) {
            clearScreen();
            printHeader();
            news.roll();
            mkt.print(true);

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
                case "." -> {
                    System.out.print("---Stock: ");
                    int stockNum = Integer.parseInt(scanner.nextLine());
                    clearLine();
                    String msg = usr.buy(scanner, mkt, stockNum);
                    if (!msg.equals("")) {
                        System.out.println(yellow(msg));
                        pause(2000);
                    }
                }
                default -> {
                    System.out.println(red("Invalid Command"));
                    pause(2000);
                }
            }
        }
    }
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
                    if (Integer.parseInt(view) < Market.MAX_STOCKS) {
                        view = "" + (Integer.parseInt(view) + 1);
                    }
                    else {
                        view = "" + 1;
                    }
                }
                case "." -> {
                    String msg = usr.buy(scanner, mkt, Integer.parseInt(view));
                    if (!msg.equals("")) {
                        System.out.println(yellow(msg));
                        pause(1000);
                    }
                }
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

    void advance() {
        mkt.advance();
        usr.update();
        news.update();
    }
    void advance(int times) {
        for (int i = 0; i < times; i++) {
            this.advance();
        }
    }
    // specific advance for init case (only affects market values)
    private void initAdvance() {
        mkt.advance();
    }
    
    // save functions
    private void saveGame() {
        mkt.save();
        usr.save();
    }
    private void loadGame(String username) throws InitException {
        mkt.load();
        usr.load(username, mkt);
    }
    private void createSaveFile(String username) {
        // check dependencies
        try {
            mkt.load();
        }
        catch (InitException e) {
            createGen();
        }

        usr = new User(username, User.STARTING_CASH, news, this);
        usr.save();
    }

    // menu functions
    private void showMainMenu() {
        clearScreen();
        printHeader();

        // main menu options
        System.out.println(center("Welcome to the show!", MENU_WIDTH));
        System.out.println(italic(center("Version 0.12 beta", MENU_WIDTH)));

        // main menu art
        String filePath;
        if (Main.NDEV) {
            filePath = "C:\\Program Files\\Terminal Trader\\assets\\city-skyline100.txt";
        }
        else {
            filePath = "assets\\city-skyline100.txt";
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(center(line, MENU_WIDTH));
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }

        // the rest
        System.out.println("-".repeat(MENU_WIDTH));
        System.out.println(cyan("1. Load Game | 2. New Game | 3. Extras | 4. Exit"));
        System.out.println("-".repeat(MENU_WIDTH));
        System.out.print("Please make a selection: ");
    }
    private void showTurnMenu() {
        clearScreen();
        printHeader();
        news.roll();
        System.out.println("-".repeat(MENU_WIDTH));

        System.out.println("1. Your Portfolio");
        System.out.println("2. Stock Exchange");
        System.out.println("3. Your Stats");
        System.out.println("4. Achievements");
        System.out.println("5. News Feed");
        System.out.println("6. Save and Exit");
    }
    private void showNewGameMenu() {
        clearScreen();
        printHeader();
        System.out.println("");
        System.out.println(italic(center("<----------New Game---------->", MENU_WIDTH)));
        System.out.println("");
        System.out.print(" ".repeat(30) + "Select a username: ");
    }
    private void showLoadGameMenu() {
        clearScreen();
        printHeader();
        
        // funny bit for now
        Random rand = new Random();
        if (rand.nextInt(100) == 1) {
            System.out.println("");
            printTaxMan();
        }
        System.out.println("");
        System.out.println(italic(center("<----------Load Game---------->", MENU_WIDTH)));
        System.out.println("");
        System.out.print(" ".repeat(30) + "Select a username: ");
    }

    // extras
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
    private void eMiniGames(Scanner scanner) {
        clearScreen();
        printHeader();
        System.out.println(center(" Mini Games ", MENU_WIDTH, "!"));
        System.out.println("");
        System.out.println(italic(center("Coming soon!", MENU_WIDTH)));
        System.out.println("");
        System.out.println(""); // spacing
        pause(scanner);
    }
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

    // dev tools
    private void createGen() {
        // builds save files for stocks and market
        mkt = new Market(news);

        // prep market
        mkt.setTrend(0); // for now FIXME
        mkt.setNumStocks(10);

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
            Stock s10 = new Stock(9, "Samuel's Johns", 420.00, 2.7, 3);
            Stock s11 = new Stock(9, "Jenna Gyms", 420.00, 2.7, 3);
            Stock s12 = new Stock(9, "Rockford Mine", 420.00, 2.7, 3);
            Stock s13 = new Stock(9, "Colin Call-Center", 420.00, 2.7, 3);
            Stock s14 = new Stock(9, "Dave's Diner", 420.00, 2.7, 3);

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
        } 
        catch (InitException e) {
            System.err.println("Exception In: createGen()");
            System.err.println("This: " + e.getMessage());
        }

        mkt.save();

        try { 
            mkt.load(); // mkt should look for s0-9 because i set numStocks to 10
        } 
        catch (InitException e) { 
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }

        // advance time to generate histories
        advance(100);

        // save files
        mkt.save();
        news.clear(); // clear acv caused by advancing
    }
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