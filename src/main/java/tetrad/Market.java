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
import static tetrad.Mutil.center;
import static tetrad.Mutil.numColor;

class Market {
    private final Stock[] stocks;
    private int trend;
    private int numStocks; // number of stocks that will be loaded from file
    private final News channel; // reference to news object for pushing alerts

    // the total possible amount of stocks, >= numStocks
    static final int MAX_STOCKS = 10;

    Market(News channel) {
        // call load() method to init rest
        stocks = new Stock[MAX_STOCKS];
        this.channel = channel;
    }

    // gameplay functions
    void advance() {
        for (int i = 0; i < numStocks; i++) {
            stocks[i].advance();
        }
    }

    // display functions
    void print(boolean isSelect) {
        System.out.println(center("Market", MENU_WIDTH, "~"));
        System.out.println("");

        // Check if there are stocks to display
        if (numStocks == 0) {
            System.out.println("No stocks available in the market.");
            return;
        }

        // Print the header
        String header = "";
        int offset = isSelect ? 5 : 0; // Offset for selection index
        if (isSelect) {
            header = add(header, "ID", 0);
        }
        header = add(header, "Stock Name", 5 + offset);
        header = add(header, "Current Price", 25 + offset);
        header = add(header, "Recent", 45 + offset);
        header = add(header, "10-Day", 60 + offset);
        System.out.println(header);
        System.out.println(""); // spacing

        // Print each stock's information
        for (int i = 0; i < numStocks; i++) {
            Stock stock = stocks[i];
            if (stock != null) {
                String line = "";

                // Add index if applicable
                if (isSelect) {
                    line += (i + 1) + ". ";
                }

                try {
                    // Add formatted stock details
                    line = add(line, stock.getName(), 5 + offset); // Stock Name
                    line = add(line, String.format("$%.2f", stock.getValue()), 25 + offset); // Current Price
                    line = add(line, numColor(String.format("%.2f", stock.getChange())), 45 + offset); // Recent Change
                    line = add(line, numColor(String.format("%.2f", stock.getChange(10))), 69 + offset); // 10-Day Change
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
    void print() {
        print(false);
    }

    // getters
    int getTrend() { return trend; }
    Stock getStock(int id) { return stocks[id]; }

    // setters
    void setTrend(int trend) { this.trend = trend; }
    void setNumStocks(int num) { this.numStocks = num; }

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
            writer.println("DATA: trend number_of_stocks");
            writer.print(trend + " ");
            writer.print(numStocks + " ");
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

            trend = Integer.parseInt(scanner.next());
            numStocks = Integer.parseInt(scanner.next());
            scanner.nextLine(); // consume newline char

            // load all stocks
            for (int i = 0; i < numStocks; i++) {
                Stock stock = new Stock(this, channel);
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