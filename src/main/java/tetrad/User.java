package tetrad;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static tetrad.Mutil.DB_LOG;
import static tetrad.Mutil.MENU_WIDTH;
import static tetrad.Mutil.add;
import static tetrad.Mutil.blue;
import static tetrad.Mutil.bold;
import static tetrad.Mutil.center;
import static tetrad.Mutil.clearLine;
import static tetrad.Mutil.clearScreen;
import static tetrad.Mutil.cyan;
import static tetrad.Mutil.dollar;
import static tetrad.Mutil.italic;
import static tetrad.Mutil.numColor;
import static tetrad.Mutil.pause;
import static tetrad.Mutil.printHeader;
import static tetrad.Mutil.red;
import static tetrad.Mutil.yellow;

class User {
    private String name; // name of the user
    private double cash; // total amount of liquid cash
    private int advances; // number of advances user has done
    private Portfolio portfolio; // portfolio of share holdings

    private final News channel; // reference to news object for pushing alerts
    private Achievements acv; // keeps track of achievements for user
    private Game game; // reference back to the current game for advancing while in portfolio

    static final double STARTING_CASH = 1000.0;

    User(News channel, Game game) {
        this("null", 0, channel, game);
    }
    User(String name, double cash, News channel, Game game) {
        this.name = name;
        this.cash = cash;
        this.channel = channel;
        this.game = game;
        
        advances = 0;
        portfolio = new Portfolio(this);
        acv = new Achievements(this, channel);
    }
    
    // gameplay functions
    // updates values after a game advance
    void update() {
        portfolio.update();
        advances++;
        acv.advanceCheck();
    }
    // takes scanner to get stock selection for sale
    // sale comes from portfolio
    String sell(Scanner scanner) {
        String transactMsg;
        while(true) {
            try {
                System.out.println("(0 to Exit)");
                System.out.print("--Stock: ");
                int selection = Integer.parseInt(scanner.nextLine());
                if (selection == 0) {
                    return ""; // exit case
                }

                System.out.print("---Amount: ");
                int amount = Integer.parseInt(scanner.nextLine());
                if (amount == 0) {
                    return ""; // exit case
                }

                Stock stock = portfolio.stockAt(selection - 1);
                portfolio.remove(stock, amount); // may throw
                cash += stock.getValue() * amount;
                transactMsg = "Sold " + amount + " shares of " + stock.getName() + " for " + dollar(stock.getValue() * amount);
                break; // won't execute if exception throws
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
        portfolio.update(false);
        acv.sellCheck();
        // takes scanner to get stock selection for purchase
        // purchase stock comes from market
        // returns String confirmation of transaction
        return transactMsg;
    }
    String buy(Scanner scanner, Market market, int selection) {
        String transactMsg;
        while(true) {
            try {
                // show cash and max buy amount
                Stock stock = market.getStock(selection - 1);
                int maxAmount = (int) (cash / stock.getValue());
                System.out.println(yellow(bold("Total Cash: ")) + dollar(cash) + " | " 
                + yellow(bold("Max Amount: ")) + maxAmount);
                System.out.println("-".repeat(MENU_WIDTH));

                System.out.print("---Amount: ");
                int amount = Integer.parseInt(scanner.nextLine());
                if (amount == 0) {
                    return ""; // exit case
                }

                // market is sorted by id
                if (cash - stock.getValue() * amount < 0) {
                    throw new InvalidSelectionException("Insufficient funds");
                }

                portfolio.add(stock, amount);
                cash -= stock.getValue() * amount;
                transactMsg = "Purchased " + amount + " shares of " + stock.getName() + " for " + dollar(stock.getValue() * amount);
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
        portfolio.update(false);
        acv.buyCheck();
        return transactMsg;
    }
    void showPortfolio(Scanner scanner) {
        try {
            while (true) {
                clearScreen();
                printHeader();
                channel.roll();

                System.out.println(center(" " + name + "'s Portfolio ", MENU_WIDTH, "="));
                portfolio.printHistory();
                System.err.println("=".repeat(MENU_WIDTH));
                
                if (portfolio.size() == 0) {
                    System.out.println(""); // spacing
                    System.out.println("Your portfolio is currently empty,");
                    System.out.println("Head to the Stock Exchange to purchase shares!");
                    System.out.println(""); // spacing
                }
                else {
                    String header = "";
                    int offset = 5;
                    header += " ".repeat(offset); // spacing for indexes
                    header = add(header, "Stock Name", offset);
                    header = add(header, "Amount", 20 + offset);
                    header = add(header, "Total", 33 + offset);
                    header = add(header, "Current", 43 + offset);
                    header = add(header, "Recent", 60 + offset);
                    header = add(header, "Gain", 75 + offset);
                    System.out.println(bold(header));

                    // print all holdings
                    for (int i = 0; i < portfolio.size(); i++) {
                        Stock  stock = portfolio.stockAt(i);
                        String index  = "";
                        String sname  = stock.getName();
                        String amount = "";
                        String total  = "";
                        String current = "";
                        String change = "";
                        String gain = "";
                        String line   = ""; // formatting

                        // print indexes for selecting stock (if applicable)
                        
                        index = (i + 1) + ". ";
                        line += index; // add to line

                        // format name (max length 20)
                        line = add(line, sname, offset);

                        // format amount (max length 10)
                        amount += center(portfolio.amountAt(i), 8);
                        line = add(line, amount, 20 + offset); // add to line

                        // format total (max length 12)
                        total += dollar(portfolio.amountAt(i) * stock.getValue());
                        line = add(line, total, 32 + offset);

                        // format current
                        current +=  dollar(stock.getValue());
                        line = add(line, current, 44 + offset);

                        // format percent change
                        change += String.format("%.2f", stock.getChange());
                        // color accordingly
                        change = numColor(change);
                        line = add(line, change, 60 + offset);

                        // format gain
                        gain += String.format("%.2f", portfolio.calculateGain(i));
                        gain = numColor(gain);
                        line = add(line, gain, 83 + offset);
                        System.out.println(line); // output final line
                    }
                }
                
                // print other information
                System.out.println("-".repeat(MENU_WIDTH));
                System.out.println(
                    red(bold("Day " + advances)) + " | " +
                    yellow(bold("Total Cash: ") + dollar(cash)) + " | " +
                    blue(bold("Total Holdings: ") + dollar(portfolio.getValue())) + " | " +
                    cyan(bold("Net Worth: ") + dollar(getNet()))
                );

                // command tray
                System.out.println("-".repeat(MENU_WIDTH));
                System.out.println(cyan("Enter - Exit") + " | " 
                + cyan("'/' - Advance") + " | " 
                + cyan("'.' - Sell") + " | "
                + cyan("; - History"));
                System.out.println("-".repeat(MENU_WIDTH));

            
                String command = scanner.nextLine();    
                clearLine();
                switch (command) {
                    case "" -> {
                        return;
                    }
                    case "/" -> {
                        game.advance();
                        pause(500);
                    }
                    case "." -> {
                        if (portfolio.size() == 0) {
                            System.out.println(red("Nothing to sell"));
                            pause(scanner);
                        }
                        else {
                            String msg = sell(scanner);
                            if (!msg.equals("")) {
                                System.out.println(yellow(msg));
                                pause(2000);
                            }
                        }
                    }
                    case ";" -> {
                        clearScreen();
                        printHeader();
                        System.out.println(center("Transaction History", MENU_WIDTH, "="));
                        portfolio.printTransactionLogs();
                        System.out.println(""); // spacing
                        pause(scanner);
                    }
                    default -> {
                        System.out.println(red("Invalid Command"));
                        pause(scanner);
                    }
                }
            }
        }
        catch (InvalidSelectionException e) {
            System.err.println("game.usr.portfolio.size improperly set");
        }
    }
    void showAchievements() {
        acv.printPage();
    }
    void showStats() {
        String traderScore;
        if (advances == 0) {
            traderScore = "N/A";
        }
        else {
            traderScore = "" + ((int) (getNet() - STARTING_CASH)/advances);
        }

        System.out.println(""); // spacing
        if (Game.ARCADE_MODE) {
            System.out.println(red(italic(center("Arcade  Mode", MENU_WIDTH))));
        }
        System.out.println(bold(center("Trader Score", MENU_WIDTH)));
        System.out.println(center(traderScore, MENU_WIDTH));
        System.out.println("");
        System.out.println(bold(center("Advances", MENU_WIDTH)));
        System.out.println(center(advances, MENU_WIDTH));
        System.out.println("");
        System.out.println(bold(center("Gains", MENU_WIDTH)));
        System.out.println(center(dollar(getNet() - STARTING_CASH), MENU_WIDTH));
        System.out.println(""); // spacing
    }
    
    // getters
    String getName() { return name; }
    double getCash() { return cash; }
    double getNet() { return portfolio.getValue() + cash; }
    int    getAdvances() { return advances; }
    Portfolio getPortfolio() { return portfolio; }

    // setters
    void setName(String name) { this.name = name; }
    void setCash(double cash) { this.cash = cash; }
    void setAdvances(int adv) { advances = adv; }

    // save functions
    void save() {
        // determine correct save path
        String fileName;
        if (Main.NDEV) {
            String savePath = System.getenv("APPDATA") + "\\Terminal Trader\\saves\\";
            fileName = savePath + name + ".txt";
        }
        else {
            fileName = "saves/" + name + ".txt";
        }

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(fileName));
            writer.println("---USER-INFO---");
            writer.println(name);
            writer.println(cash);
            writer.println(advances);
    
            // portfolio
            portfolio.save();
            writer.println("---INFO-END---");
    
            // label
            writer.println("For Terminal Trader, a text-based stock game.");
            writer.println("Developed and Designed by Samuel Johns");
    
            writer.close();
        } 
        catch (IOException e) {
            DB_LOG("IO Error: User Save Method -> " + e.getMessage());
        }
        
        portfolio.save();
        acv.save();
    }
    void load(String username, Market market) throws InitException {
        // determine correct save path
        String fileName;
        if (Main.NDEV) {
            String savePath = System.getenv("APPDATA") + "\\Terminal Trader\\saves\\";
            fileName = savePath + username + ".txt";
        }
        else {
            fileName = "saves/" + username + ".txt";
        }

        File file = new File(fileName);
        try (Scanner scanner = new Scanner(file)) {
            this.name = username;
            // skip header
            scanner.nextLine();
            name = scanner.nextLine();
            cash = Double.parseDouble(scanner.nextLine());
            advances = Integer.parseInt(scanner.nextLine());

            portfolio.load(market);
            acv.load();

            portfolio.update(false); // update value for init
        }
        catch (NoSuchElementException e) {
            throw new InitException("Corrupted Stock Data");
        }
        catch (FileNotFoundException e) {
            throw new InitException("File Not Found: " + username + ".txt");
        }
    }
}
