package tetrad;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static tetrad.Mutil.DB_LOG;
import static tetrad.Mutil.dollar;
import static tetrad.Mutil.pause;

class User {
    protected String name; // name of the user
    protected double cash; // total amount of liquid cash
    protected int advances; // number of advances user has done
    protected Portfolio portfolio; // portfolio of share holdings

    protected final News channel; // reference to news object for pushing alerts
    protected Achievements acv; // keeps track of achievements for user
    protected Game game; // reference back to the current game for advancing while in portfolio

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

    // DO-NOTHINGS (not relevant for base class)
    String buy(Scanner scn, Market mkt, int sel) { return ""; }
    
    String sell(Stock stock, int amount) { 
        String transactMsg = "";

        try {
            portfolio.remove(stock, amount); // may throw
            cash += stock.getValue() * amount;
            transactMsg = "Sold " + amount + " shares of " + stock.getName() + " for " + dollar(stock.getValue() * amount);
        }
        catch (NoSuchElementException | NumberFormatException e) {
            System.out.println("Invalid Input, please try again.");
            pause(1000);
        }
        catch (InvalidSelectionException e)  {
            System.out.println(e.getMessage());
            pause(1000);
        }
        
        portfolio.update(false);
        acv.sellCheck();

        return transactMsg;
    }
    void showPortfolio(Scanner scanner) {}
    void showAchievements() {}
    void showStats() {}
}
