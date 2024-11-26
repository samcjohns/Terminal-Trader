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
 */

class Market {
    private final Stock[] stocks; // all stocks objects
    static int NUM_STOCKS = 10;   // number of stocks on the market
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
    double      getTrend() { return trend; }
    double        getVol() { return vol; }
    Stock getStock(int id) { return stocks[id]; }

    // setters
    void    setTrend(int trend) { this.trend = trend; }
    void        setVol(int vol) { this.vol = vol; }
    void setNUM_STOCKS(int num) { NUM_STOCKS = num; }

    // save functions
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
}