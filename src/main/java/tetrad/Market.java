package tetrad;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

import static tetrad.Mutil.DB_LOG;
import static tetrad.Mutil.MENU_WIDTH;
import static tetrad.Mutil.add;
import static tetrad.Mutil.center;
import static tetrad.Mutil.numColor;

/**
 * A specialized container for stock objects
 * 
 * @author Samuel Johns
 * Created: November 15, 2024
 *
 * Description: The Market class is responsible for storing each stock object.
 *              Since there can be market wide behavior, the market has
 *              behvavioral variables for stock advances. When the market saves
 *              and loads, it is responsible for calling each stock save and
 *              load method as well.
 * 
 * @see Stock
 */

class Market {
    private final Stock[] stocks; // all stocks objects
    static int NUM_STOCKS = 15;   // number of stocks on the market
    private final Game game;      // reference to current game instance

    // behavioral variables for market
    private double trend; // current trend
    private double vol;   // current volatility

    Market(Game game) {
        // call load() method to init rest
        stocks = new Stock[NUM_STOCKS];
        this.game = game;
    }

    // gameplay functions
    /**
     * Advances market state to next day
     * updates trend, volatility, and calls advance methods for each stock
     */
    void advance() {
        Random rand = new Random();
        
        vol += rand.nextDouble() * 0.1;           // slightly adjust volatility

        double change = rand.nextDouble() / 10;   // get a random number between 0 and 0.1
        change *= vol;                            // incorporate market volatility
        change += (-trend) * 0.1;                 // correct trend with factor of 0.1
        trend += change;                          // incorporate change
        trend = Math.min(3, Math.max(trend, -3)); // limit trend
    
        // advance all stocks
        for (int i = 0; i < NUM_STOCKS; i++) {
            stocks[i].advance();
        }
    }

    // getters
    /**
     * @return market trend
     */
    double      getTrend() { return trend; }
    /**
     * @return market volatility
     */
    double        getVol() { return vol; }
    /**
     * @param id stock id
     * @return stock with given id
     */
    Stock getStock(int id) { return stocks[id]; }

    // setters
    /**
     * Sets market trend
     * @param trend new market trend
     */
    void    setTrend(int trend) { this.trend = trend; }
    /**
     * Sets market volatility
     * @param vol new market volatility
     */
    void        setVol(int vol) { this.vol = vol; }
    /**
     * Sets static number of ingame stocks
     * @param num new total number of stocks
     */
    void setNUM_STOCKS(int num) { NUM_STOCKS = num; }

    // save functions
    /**
     * Saves market to "mkt.txt"
     */
    void save() {
        // saves all info about the market to a unique file
        try {
            // determine correct save path
            String fileName;
            if (Main.NDEV) {
                String savePath = System.getenv("APPDATA") + "\\Terminal Trader\\gen\\";
                fileName = savePath + "mkt.txt";
            }
            else {
                fileName = "gen/mkt.txt";
            }

            PrintWriter writer = new PrintWriter(new FileWriter(fileName));

            writer.println("---MARKET-INFO---");

            // attributes
            writer.println("---ATTRIBUTES---");
            writer.println("DATA: trend vol number_of_stocks");
            writer.print(trend + " ");
            writer.print(vol + " ");
            writer.print(NUM_STOCKS + " ");
            writer.println(""); // newline
            writer.println("---INFO-END---");

            // label
            writer.println("For Terminal Trader, a text-based stock game.");
            writer.println("Developed and Designed by Samuel Johns");

            writer.close();
        } 
        catch (IOException e) {
            DB_LOG("IO Error: Market Save Method -> " + e.getMessage());
        }

        // init case
        if (stocks[0] == null) {
            return;
        }

        // save all stocks
        for (Stock s : stocks) {
            s.save();
        }
    }
    /**
     * Loads market from "mkt.txt"
     * @throws InitException if file is corrupted or missing
     */
    void load() throws InitException {
        // determine correct save path
        String fileName;
        if (Main.NDEV) {
            String savePath = System.getenv("APPDATA") + "\\Terminal Trader\\gen\\";
            fileName = savePath + "mkt.txt";
        }
        else {
            fileName = "gen/mkt.txt";
        }

        File file = new File(fileName);
        try (Scanner scanner = new Scanner(file)) {
            // skip headers
            scanner.nextLine();
            scanner.nextLine();
            scanner.nextLine();

            trend = Double.parseDouble(scanner.next());
            vol = Double.parseDouble(scanner.next());
            NUM_STOCKS = Integer.parseInt(scanner.next());
            scanner.nextLine(); // consume newline char

            // load all stocks
            for (int i = 0; i < NUM_STOCKS; i++) {
                Stock stock = new Stock(this, game);
                stock.load(i);
                stocks[i] = stock;
            }
        }
        catch (NoSuchElementException e) {
            throw new InitException("Corrupted Stock Data");
        }
        catch (FileNotFoundException e) {
            throw new InitException("File Not Found: " + file);
        }
    }

    // text functions
    /**
     * Prints info about the whole market, previews about stock prices, etc.
     */
    void print() {
        // header
        System.out.println(center("Market", MENU_WIDTH, "~"));
        System.out.println("");

        // Print the header
        String header = "";
        
        header = add(header, "ID", 0);
        header = add(header, "Stock Name", 10);
        header = add(header, "Current Price", 30);
        header = add(header, "Recent", 50);
        header = add(header, "10-Day", 65);

        System.out.println(header);
        System.out.println(""); // spacing

        // Print each stock's information
        for (int i = 0; i < Market.NUM_STOCKS; i++) {
            Stock stock = stocks[i];
            if (stock != null) {
                String line = "";
                line += (i + 1) + ". ";

                try {
                    // Add formatted stock details
                    line = add(line, stock.getName(), 10); // Stock Name
                    line = add(line, String.format("$%.2f", stock.getValue()), 30); // Current Price
                    line = add(line, numColor(String.format("%.2f", stock.getChange())), 50); // Recent Change
                    line = add(line, numColor(String.format("%.2f", stock.getChange(10))), 74); // 10-Day Change
                }
                catch (IllegalArgumentException e) {
                    // some number is unexpectedly large
                    // stop printing line
                }
                
                System.out.println(line); // Output the line for each stock
            } 
            else {
                System.out.println("Stock information not available.");
            }
        }

        System.out.println(""); // spacing
        System.out.println("~".repeat(MENU_WIDTH));
    }
}