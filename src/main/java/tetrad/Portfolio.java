/**
 * Class: Portfolio
 * Summary: Data Structure for Stocks Owned 
 * 
 * @Author: Samuel Johns
 * @Date: October 29, 2024
 * 
 * Description: A data structure for storing a Stock-Int pair, with some added
 *              functionality. Pair is stored by total valuation.
 */
package tetrad;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static tetrad.Market.MAX_STOCKS;
import static tetrad.Mutil.DB_LOG;
import static tetrad.Mutil.HISTORY_HEIGHT;
import static tetrad.Mutil.dollar;
import static tetrad.Mutil.round;

class Portfolio {
    private final User owner; // owner of this portfolio
    private final Stock[] stocks;
    private final int[] amounts;
    private final double[] prices; // original prices

    private double value; // total valuation of all stocks
    private Log<Double> history; 
    private Log<Transaction> transactions; // history of transactions

    private int size; // amount of unique stocks owned

    Portfolio(User usr) {
        owner = usr;
        stocks = new Stock[MAX_STOCKS];
        amounts = new int[MAX_STOCKS];
        prices = new double[MAX_STOCKS];
        history = new Log<>(Log.DEFAULT_SIZE);
        transactions = new Log<>(Log.DEFAULT_SIZE);
        
        value = 0;
        size = 0;
    }

    int size() { return size; }
    boolean isEmpty() { return size == 0; }
    double getValue() { return value; }
    double getLast() { return history.at(history.size()-1); }

    // returns recent change in value (total portfolio)
    double getChange() {
        return round(((value - getLast()) / getLast()) * 100);
    }

    // updates valuations and history
    void update(boolean push) {
        // push = true for advancing the game
        // push = false for updating after transaction
        if (push) {
            // history is based on net worth
            history.push(round(value + owner.getCash()));
        }

        value = 0;
        for (int i = 0; i < size; i++) {
            value += stocks[i].getValue() * amounts[i];
        }
        value = round(value); // for cleanliness
        sort();
    }
    void update() {
        this.update(true);
    }

    // helps find stock in sorted array
    Stock stockAt(int i) throws InvalidSelectionException {
        if (i < stocks.length) {
            return stocks[i];
        }
        else {
            throw new InvalidSelectionException("Stock not present.");
        }
    }
    int amountAt(int i) throws InvalidSelectionException { 
        if (i < amounts.length) {
            return amounts[i];
        }
        else {
            throw new InvalidSelectionException("Invalid Input");
        }
    }

    // Adds stock after purchase
    void add(Stock newStock, int addAmount, double price) {
        // Search for stock
        for (int i = 0; i < size; i++) {
            // Case 1: Stock exists, update amount and weighted average price
            if (stocks[i] == newStock) {
                double totalCost = prices[i] * amounts[i] + price * addAmount; // Calculate total cost for stock
                amounts[i] += addAmount; // Update the total shares
                prices[i] = totalCost / amounts[i]; // Update the weighted average purchase price
                sort();
                return;
            }
        }

        stocks[size] = newStock;
        amounts[size] = addAmount;
        prices[size] = price; // Store the purchase price as the initial price
        size++;
        sort();

        // add transaction to log
        Transaction trn = new Transaction(newStock, addAmount, price);
        transactions.push(trn);
    }
    void add(Stock newStock, int addAmount) {
        this.add(newStock, addAmount, newStock.getValue());
    }
    // Removes stock after sale
    void remove(Stock stock, int amount) throws InvalidSelectionException {
        // Search for stock
        for (int i = 0; i < size; i++) {
            if (stocks[i] == stock) {
                // Case 1: Stock exists, sufficient amount
                if (amounts[i] >= amount) {
                    amounts[i] -= amount; // Decrease the amount of stock
                    if (amounts[i] == 0) {
                        removeStock(i); // Remove stock entirely if no more shares
                    }
                    sort();

                    // add transaction to log
                    Transaction trn = new Transaction(stock, -amount, stock.getValue());
                    transactions.push(trn);
                    return;
                }

                // Case 2: Stock exists, insufficient amount
                else {
                    throw new InvalidSelectionException("Not enough stocks to sell.");
                }
            }
        }

        // Case 3: Stock doesn't exist
        throw new InvalidSelectionException("Invalid Stock Selected");
    }

    // helper functions
    private void sort() {
        for (int i = 0; i < size; i++) {
            double maxValue = -1;
            int maxIndex = -1;
            for (int j = i; j < size; j++) {
                if (stocks[j].getValue() > maxValue) {
                    maxValue = stocks[j].getValue();
                    maxIndex = j;
                }
            }
            move(maxIndex, i);
        }
    }
    private void removeStock(int i) {
        stocks[i] = null;
        amounts[i] = -1;
        prices[i] = 0;
        move(i, --size); // swap with last item to fill hole
    }
    private void move(int index, int newIndex) {
        if (index == newIndex) {
            return;
        }

        // swap index and newIndex
        Stock tempStock = stocks[newIndex];
        int tempAmount = amounts[newIndex];
        double tempPrice = prices[newIndex];

        stocks[newIndex] = stocks[index];
        amounts[newIndex] = amounts[index];
        prices[newIndex] = prices[index];

        stocks[index] = tempStock;
        amounts[index] = tempAmount;
        prices[index] = tempPrice;
    }
    // Calculate gain or loss for each stock
    double calculateGain(int index) throws InvalidSelectionException {
        if (index < size) {
            double currentValue = stocks[index].getValue(); // Get the current value of the stock
            double averagePurchasePrice = prices[index]; // Get the average purchase price for this stock
            int amountOwned = amounts[index]; // Get the amount of stock owned
            
            // Only calculate gain if there are stocks owned
            if (amountOwned > 0) {
                // Calculate the percentage gain/loss based on current value and purchase price
                return ((currentValue - averagePurchasePrice) / averagePurchasePrice) * 100;
            } else {
                return 0; // No stocks owned, gain is zero
            }
        } else {
            throw new InvalidSelectionException("Invalid stock index.");
        }
    }

    void printTransactionLogs() {
        for (int i = 0; i < transactions.size(); i++) {
            System.out.println(""); // spacing
            System.out.println("Stock: " + transactions.at(i).getStock().getName());
            System.out.println("Amount: " + transactions.at(i).getAmount());
            System.out.println("At: " + dollar(transactions.at(i).getPrice()));
        }
    }
    void printHistory() {
        final int ROWS = HISTORY_HEIGHT;       // max graph height
        final int COLS = history.size();       // max graph length
        char[][] array = new char[ROWS][COLS]; // for building graph
    
        // Fill the array with spaces
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                array[i][j] = ' ';
            }
        }

        // Find upper and lower bounds
        double minVal = Double.POSITIVE_INFINITY;
        double maxVal = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < history.size(); i++) {
            double val = history.at(i);
            if (val > maxVal) {
                maxVal = val;
            }
            if (val < minVal) {
                minVal = val;
            }
        }

        // Determine bins for graph height
        double range = maxVal - minVal;
        double binSize = range / ROWS;
    
        // Build graph in array
        for (int i = 0; i < history.size(); i++) {
            // Calculate height of the bar based on current value
            int barHeight = (int) ((history.at(i) - minVal) / binSize);
    
            // Ensure barHeight is within the bounds of the array
            barHeight = Math.min(barHeight, ROWS - 1);
            barHeight = Math.max(barHeight, 0); // This will be at least 0
            
            // Fill the graph from the bottom up
            for (int j = barHeight; j >= 0; j--) {
                array[ROWS - j - 1][COLS - i - 1] = '█'; // Fill upwards, ensuring the lowest index is the bottom
            }
        }
    
        // Print graph
        for (int i = 0; i < ROWS; i++) { // Print from top to bottom
            for (int j = 0; j < COLS; j++) {
                System.out.print(array[i][j]);
            }
            System.out.println();
        }
    }
    void save() {
        // determine correct save path
        String fileName;
        if (Main.NDEV) {
            String savePath = System.getenv("APPDATA") + "\\Terminal Trader\\saves\\";
            fileName = savePath + owner.getName() + "_portfolio.txt";
        }
        else {
            fileName = "saves/" + owner.getName() + "_portfolio.txt";
        }
        
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(fileName));
            writer.println("---PORTFOLIO-INFO---");

            // stock info
            writer.println("DATA: stockID amount originalPrice");
            for (int i = 0; i < size; i++) {
                writer.print(stocks[i].getID() + " ");
                writer.print(amounts[i] + " ");
                writer.print(prices[i]);
                writer.println(""); // newline
            }

            // portfolio valuation info
            writer.println("---HISTORY---");
            for (int i = history.size() - 1; i >= 0; i--) {
                // print in reverse order for when it loads
                writer.print(history.at(i) + " ");
            }
            writer.println(""); // newline
            writer.println("---HISTORY-END---");

            // transaction log info
            writer.println("---TRANSACTIONS-LOG---");
            for (int i = transactions.size() - 1; i >= 0; i--) {
                // print in reverse order for when it loads
                Transaction t = transactions.at(i);
                int stockID = t.getStock().getID();
                int amount = t.getAmount();
                double price = t.getPrice();
                writer.println(stockID + " " + amount + " " + price);
            }
            writer.println(""); // newline
            writer.println("---TRANSACTIONS-END---");
            
            writer.println("---INFO-END---");
    
            // label
            writer.println("For Terminal Trader, a text-based stock game.");
            writer.println("Developed and Designed by Samuel Johns");
    
            writer.close();
        } 
        catch (IOException e) {
            DB_LOG("IO Error: Portfolio Save Method -> " + e.getMessage());
        }
    }
    void load(Market market) throws InitException {
        // determine correct save path
        String fileName;
        if (Main.NDEV) {
            String savePath = System.getenv("APPDATA") + "\\Terminal Trader\\saves\\";
            fileName = savePath + owner.getName() + "_portfolio.txt";
        }
        else {
            fileName = "saves/" + owner.getName() + "_portfolio.txt";
        }

        File file = new File(fileName);
        try (Scanner scanner = new Scanner(file)) {
            // skip header
            scanner.nextLine();
            scanner.nextLine();
            
            String line = scanner.nextLine();
            while(!line.equals("---HISTORY---")) {
                Scanner lineScanner = new Scanner(line);
                int stockID = lineScanner.nextInt();
                int amount = lineScanner.nextInt();
                double price = lineScanner.nextDouble();
                lineScanner.close();

                add(market.getStock(stockID), amount, price);

                line = scanner.nextLine();
            }

            String token = scanner.next();
            while (!token.equals("---HISTORY-END---")) {
                history.push(Double.parseDouble(token));
                token = scanner.next();
            }

            // transactions
            line = scanner.nextLine();
            while (!line.equals("---TRANSACTIONS-END---")) {
                // transactions is empty
                if (line.equals("")) {
                    break;
                }
                Scanner lineScanner = new Scanner(line);
                int stockID = lineScanner.nextInt();
                int amount = lineScanner.nextInt();
                double price = lineScanner.nextDouble();
                Transaction t = new Transaction(market.getStock(stockID), amount, price);
                transactions.push(t);
                lineScanner.close();
                line = scanner.nextLine();
            }
            scanner.close();
        }
        catch (NoSuchElementException e) {
            throw new InitException("Corrupted Portfolio File");
        }
        catch (FileNotFoundException e) {
            throw new InitException("File Not Found: " + fileName);
        }
    }
}