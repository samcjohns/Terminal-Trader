package tetrad;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static tetrad.Mutil.DB_LOG;
import static tetrad.Mutil.MENU_WIDTH;
import static tetrad.Mutil.add;
import static tetrad.Mutil.blue;
import static tetrad.Mutil.bold;
import static tetrad.Mutil.center;
import static tetrad.Mutil.cursorDown;
import static tetrad.Mutil.cursorRight;
import static tetrad.Mutil.cyan;
import static tetrad.Mutil.dollar;
import static tetrad.Mutil.green;
import static tetrad.Mutil.italic;
import static tetrad.Mutil.numColor;
import static tetrad.Mutil.red;
import static tetrad.Mutil.yellow;

/**
 * Class for representing and storing information about the player.
 * 
 * @author Samuel Johns
 * Created: November 15, 2024
 * 
 * Description: The User class is for representing the player and handling
 *              behavior ingame. It stores the username, cash held, number of
 *              advances taken, the players Portfolio, the relevant
 *              Achievements instance, and the relevant game instance.
 * 
 * @see Portfolio
 * @see Achievements
 * @see Game
 */

class User {
    private String name; // name of the user
    private double cash; // total amount of liquid cash
    private int advances; // number of advances user has done
    private Portfolio portfolio; // portfolio of share holdings
    LocalDate startDate; // account start date

    /**
     * <p> True - if user is loaded in main game.
     * <p> False - if user is loaded in environment where market or news cannot
     * be initialized.
     */
    private boolean main;

    protected Achievements acv; // keeps track of achievements for user
    private Game game; // reference back to the current game for advancing while in portfolio

    static final double STARTING_CASH = 1000.0;

    User() {
        this.name = "null";
        main = false;
        acv = new Achievements(this, null);
    }
    User(Game game) {
        this("null", 0, game, null);
    }
    User(String name, double cash, Game game, LocalDate startDate) {
        this.name = name;
        this.cash = cash;
        this.game = game;
        this.startDate = startDate;
        
        main = true;
        advances = 0;
        portfolio = new Portfolio(this);
        acv = new Achievements(this, game);
    }
    
    // gameplay functions
    /**
     * Increments the number of advances, updates the porfolio, and checks for
     * achievements.
     */
    void update() {
        portfolio.update();
        advances++;
        acv.advanceCheck();
    }
    
    // getters
    /**
     * @return username of the user
     */
    String getName() { return name; }

    /**
     * @return cash held by the user
     */
    double getCash() { return cash; }

    /**
     * @return net worth of the user
     */
    double getNet() { return portfolio.getValue() + cash; }

    /**
     * @return number of advances of the user
     */
    int getAdvances() { return advances; }

    /**
     * @return user's Trader Score
     */
    int getScore() { 
        if (advances == 0) {
            return 0;
        }
        return ((int) (getNet() - STARTING_CASH)/advances); 
    }

    /**
     * @return a referece to the Portfolio of the user
     */
    Portfolio getPortfolio() { return portfolio; }

    /**
     * @return the user's start date
     */
    LocalDate getStartDate() { return startDate; }

    // setters
    /**
     * Sets the username of the user
     * @param name new username
     */
    void setName(String name) { this.name = name; }

    /**
     * Sets the cash held by the user
     * @param cash new amount of cash held
     */
    void setCash(double cash) { this.cash = cash; }

    /**
     * Sets a new start date for the user
     * @param date
     */
    void setStartDate(LocalDate date) {this.startDate = date; }

    /**
     * Returns the amount of taxable income attributed to the user for the
     * current tax period.
     * @return double of taxable income
     */
    double getTaxData() {
        return portfolio.realizedGains;
    }
    
    /**
     * Resets tax information after successful payment to the Taxman
     */
    void clearTaxData() {
        portfolio.realizedGains = 0;
    }

    // save functions
    /**
     * Saves the information of the user to <username>.txt
     */
    void save() {
        // determine correct save path
        String filePath = Main.getSource("saves");
        filePath += name + ".txt";

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(filePath));
            writer.println("---USER-INFO---");
            writer.println(name);
            writer.println(cash);
            writer.println(advances);
            writer.println(startDate);
            writer.println("---INFO-END---");
    
            // label
            writer.println("For Terminal Trader, a text-based stock game.");
            writer.println("Developed and Designed by Samuel Johns");
    
            writer.close();
        } 
        catch (IOException e) {
            DB_LOG("IO Error: User Save Method -> " + e.getMessage());
        }
        
        acv.save();
        if (main) {
            portfolio.save();
        }
    }

    /**
     * Loads the information of the user from <username>.txt
     * @param username User's name and the file from which to load from
     * @param market Market instance containing the stocks that User's
     * portfolio containS
     * @throws InitException if file is corrupted or missing
     */
    void load(String username, Market market) throws InitException {
        // determine correct save path
        String filePath = Main.getSource("saves");
        filePath += username + ".txt";

        File file = new File(filePath);
        try (Scanner scanner = new Scanner(file)) {
            this.name = username;
            // skip header
            scanner.nextLine();
            name = scanner.nextLine();
            cash = Double.parseDouble(scanner.nextLine());
            advances = Integer.parseInt(scanner.nextLine());
            startDate = LocalDate.parse(scanner.nextLine());

            acv.load();

            // won't load portfolio if market is null (in case of minigames)
            if (main) {
                portfolio.load(market);
                portfolio.update(false); // update value for init
            }
        }
        catch (NoSuchElementException e) {
            throw new InitException("Corrupted Stock Data");
        }
        catch (FileNotFoundException e) {
            throw new InitException("File Not Found: " + username + ".txt");
        }
    }

    /**
     * Loads the information of the user from <username>.txt, does not load
     * portfolio. Not to be used in main gameplay, however, it is helpful
     * for use with mini games.
     * @param username User's name and the file from which to load from
     * @throws InitException if file is corrupted or missing
     */
    void load(String username) throws InitException {
        this.load(username, null);
    }

    /**
     * Simulates a purchase made by the user, adds a number of shares to the 
     * user's portfolio and removes purchase price from the user's cash.
     * @param stock stock to be purchased
     * @param amount number of stocks to be purchased
     * @return a transaction message of the successful purchase
     * @throws InvalidSelectionException if the stock is invalid or if the user
     * does not have enough cash.
     */
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
    
    /**
     * Simulates a sale made by the user, removes a number of shares from the
     * user's portfolio and returns sale price to the user's cash.
     * @param stock
     * @param amount
     * @return
     * @throws InvalidSelectionException
     */
    String sell(Stock stock, int amount) throws InvalidSelectionException {

        portfolio.remove(stock, amount);   // may throw
        cash += stock.getValue() * amount; // return cash from sale
        
        portfolio.update(false);      // update new portfolio value
        acv.sellCheck();                   // check for achievements earned

        // transaction message
        return "Sold " + amount + " shares of " + stock.getName() + " for " + dollar(stock.getValue() * amount);
    }

    /**
     * Prints a page showing the portfolio, a graph of performance, and 
     * information about the user.
     */
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
            header = add(header, "Current", 53);
            header = add(header, "Recent", 70);
            header = add(header, "Gain", 85);
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
                    line = add(line, current, 54);

                    // format percent change
                    change += String.format("%.2f", stock.getChange());
                    // color accordingly
                    change = numColor(change);
                    line = add(line, change, 70);

                    // format gain
                    gain += String.format("%.2f", portfolio.calculateGain(i));
                    gain = numColor(gain);
                    line = add(line, gain, 93);
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

    /**
     * Prints a page showing achievements locked and unlocked for the user.
     * @param page current page of achievements
     */
    void showAchievements(int page) {
        System.out.println(""); // spacing
        System.out.println(green(center("Completed", MENU_WIDTH)));
        System.out.println(red(center("Not Completed", MENU_WIDTH)));
        System.out.println(""); // spacing
    
        int acvPerPage = 7;
        int totalPages = (int) Math.ceil(Achievements.ACV_AMOUNT / (double) acvPerPage);
    
        // Check if the page number is within the valid range (1-based index)
        if (page < 1 || page > totalPages) {
            return;
        }
    
        // Display the specified page of achievements
        int acvOnPage = 0;
        for (int i = (page - 1) * acvPerPage; i < page * acvPerPage && i < Achievements.ACV_AMOUNT; i++) {
            String title = acv.getAcvTitle(i);
            String desc = acv.getAvcDesc(i);
            // color properly, move cursor to print in middle
            if (acv.acvList[i]) {
                cursorRight((MENU_WIDTH / 2) - (title.length() / 2));
                System.out.println(bold(green(title)));
                cursorRight((MENU_WIDTH / 2) - (desc.length() / 2));
                System.out.println(green(desc));
            } 
            else {
                cursorRight((MENU_WIDTH / 2) - (title.length() / 2));
                System.out.println(bold(red(title)));
                cursorRight((MENU_WIDTH / 2) - (desc.length() / 2));
                System.out.println(red(desc));
            }
            System.out.println(""); // spacing
            acvOnPage++;
        }
    
        // Add space to fit the rest of the page
        for (int i = acvOnPage; i < acvPerPage; i++) {
            cursorDown(3);
        }
    }
    
    

    /**
     * Prints a page showing the stats of the user.
     */
    void showStats() {
        String traderScore;
        if (advances == 0) {
            traderScore = "N/A";
        }
        else {
            traderScore = "" + getScore();
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