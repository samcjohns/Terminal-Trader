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
import static tetrad.Mutil.cyan;
import static tetrad.Mutil.dollar;
import static tetrad.Mutil.italic;
import static tetrad.Mutil.numColor;
import static tetrad.Mutil.red;
import static tetrad.Mutil.yellow;

class User {
    private String name; // name of the user
    private double cash; // total amount of liquid cash
    private int advances; // number of advances user has done
    protected Portfolio portfolio; // portfolio of share holdings

    private Achievements acv; // keeps track of achievements for user
    private Game game; // reference back to the current game for advancing while in portfolio

    static final double STARTING_CASH = 1000.0;

    User(Game game) {
        this("null", 0, game);
    }
    User(String name, double cash, Game game) {
        this.name = name;
        this.cash = cash;
        this.game = game;
        
        advances = 0;
        portfolio = new Portfolio(this);
        acv = new Achievements(this, game.news);
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

    String buy(Stock stock, int amount) throws InvalidSelectionException {
        // check if user can afford purchase (throws if stock doesn't exist)
        if (stock.getValue() * amount > cash) {
            throw new InvalidSelectionException("Insufficient Funds");
        }

        cash -= stock.getValue() * amount; // take cash for transaction
        portfolio.add(stock, amount);      // add new stock to portfolio

        portfolio.update(false);      // update new value for portfolio
        acv.buyCheck();                    // check if achievements were earned

        return "Bought " + amount + " shares of " + stock.getName() + " for " + dollar(stock.getValue() * amount);
    }
    
    String sell(Stock stock, int amount) throws InvalidSelectionException {

        portfolio.remove(stock, amount);   // may throw
        cash += stock.getValue() * amount; // return cash from sale
        
        portfolio.update(false);      // update new portfolio value
        acv.sellCheck();                   // check for achievements earned

        // transaction message
        return "Sold " + amount + " shares of " + stock.getName() + " for " + dollar(stock.getValue() * amount);
    }
    void showPortfolio() {
        game.news.roll();

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
            header += " ".repeat(5); // spacing for indexes
            header = add(header, "Stock Name", 5);
            header = add(header, "Amount", 25);
            header = add(header, "Total", 38);
            header = add(header, "Current", 48);
            header = add(header, "Recent", 65);
            header = add(header, "Gain", 80);
            System.out.println(bold(header));

            try {
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
                    line = add(line, sname, 5);

                    // format amount (max length 10)
                    amount += center(portfolio.amountAt(i), 8);
                    line = add(line, amount, 25); // add to line

                    // format total (max length 12)
                    total += dollar(portfolio.amountAt(i) * stock.getValue());
                    line = add(line, total, 37);

                    // format current
                    current +=  dollar(stock.getValue());
                    line = add(line, current, 49);

                    // format percent change
                    change += String.format("%.2f", stock.getChange());
                    // color accordingly
                    change = numColor(change);
                    line = add(line, change, 65);

                    // format gain
                    gain += String.format("%.2f", portfolio.calculateGain(i));
                    gain = numColor(gain);
                    line = add(line, gain, 88);
                    System.out.println(line); // output final line
                }
            }
            catch(InvalidSelectionException e) {
                DB_LOG("IVE thrown in User.showPortfolio");
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
}