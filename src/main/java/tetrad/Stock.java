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
import static tetrad.Mutil.HISTORY_HEIGHT;
import static tetrad.Mutil.HISTORY_LENGTH;
import static tetrad.Mutil.round;

class Stock {
    private int id;           // unique id number
    private String name;      // name of the stock
    private double value;     // current value

    private final Log<Double> history; // past values (for printing performance graph)

    // behavior variables
    private double trend;    // how stock will act in next quarter (avg % change)
    private double vol;      // how fast the prices moves (factor)
    private int risk;        // likelihood random events

    // targets (the values variables hover around)
    private double target_value;
    private double target_vol;

    // record values for news alerts
    private double recordHigh;
    private double recordLow;
    private double alertCooldown;

    private final News channel; // reference to news object for pushing alerts
    private final Market mkt; // reference to market stock belongs to

    // constructors
    Stock(Market mkt, News channel) {
        // for typical gameplay, call this contructor and then stock.load()
        this.channel = channel;
        this.mkt = mkt;
        history = new Log<>(Log.DEFAULT_SIZE);
        alertCooldown = 3; // default
    }
    Stock(int id, String name, double target_value, double target_vol, int risk) throws InitException {
        // for creating dependencies, the first time the stock is made prior to save
        history = new Log<>(Log.DEFAULT_SIZE);
        channel = null;
        mkt = null;

        this.id = id;
        this.name = name;
        this.target_value = target_value;
        this.target_vol = target_vol;
        this.risk = risk;

        this.value = target_value;
        this.vol = target_vol;
        this.trend = 0;

        if (name.length() > 20) {
            throw new InitException("Stock name too long: " + name);
        }
    }
    
    // getters
    int           getID() { return id; }
    String      getName() { return name; }
    double     getValue() { return value; }
    double getTargetVal() { return target_value; }
    double     getTrend() { return trend; }
    double       getVol() { return vol; }

    // value functions
    double getLast() {
        return history.recent();
    }
    
    double getChange() {
        // recent change
        return round(((value - getLast()) / getLast()) * 100);
    }
    
    double getChange(int term) {
        // change from 'term' days ago
        return round(((value - history.at(term - 1)) / getLast()) * 100);
    }

    // stock behavior functions
    void advance() {
        // Method for determining stock price
        // Add old value to history
        history.push(round(value));
    
        Random rand = new Random();

        // operate with arcade stock behavior
        if (Game.ARCADE_MODE) {
            arcadeBehavior();
        }
        // operate with traditional stock behavior
        else {
            double percentChange = ((rand.nextDouble() * 5) * (1 + vol));  // 1. Random noise
            percentChange *= (rand.nextInt() % 2 == 0) ? -1 : 1;     // 2. Determine up or down
            percentChange += trend;                                  // 3. Incorporate overall trend
            value += (value * (percentChange / 100));                // 4. Apply change
            value = round(value);                                    // 5. Round to dollar value

            // trend show only have a small effect, not driving force
            trend /= 2;                                                              // incorporate previous trend
            trend += ((target_value - value)/target_value) * rand.nextDouble() * 10; // Adjust trend toward target_value
            trend = Math.max(-5, Math.min(trend, 5));                                // cap trend

            // handle random events
            if (rand.nextInt(100) < risk) {
                randomEvent(); // will apply changes to various attributes
            }
        }

        // check for record numbers
        if (value > recordHigh) {
            recordHigh = value;
            if (alertCooldown == 0) {
                Alert alert = new Alert(name + " reaches record highs.", Alert.GOOD_STOCK);
                channel.push(alert);
                alertCooldown = 4;
            }
            alertCooldown--;
        }
        else if (value < recordLow) {
            recordLow = value;
            if (alertCooldown == 0) {
                Alert alert = new Alert(name + " hits record lows", Alert.BAD_STOCK);
                channel.push(alert);
                alertCooldown = 4;
            }
            alertCooldown--;
        }
    }
    void advance(int times) {
        for (int i = 0; i < times; i ++) {
            advance();
        }
    }
    private void randomEvent() {
        Random rand = new Random();

        int outcome = rand.nextInt(5) + 1;

        // equal likelihood of all events (for now)
        switch (outcome) {
            case 1 -> {
                // stock boom
                target_value *= 1.0 + (2.0 * rand.nextDouble()); // Random value between 1.0 and 3.0
                target_value = Math.max(10.0, Math.min(1000000, target_value)); // cap
                Alert alert = new Alert("Analysts predict strong rise for " + name, Alert.GOOD_STOCK);
                channel.push(alert);
            }
            case 2 -> {
                // stock crash
                target_value /= 1.0 + (2.0 * rand.nextDouble()); // Random value between 1.0 and 3.0
                target_value = Math.max(10.0, Math.min(1000000, target_value)); // cap
                Alert alert = new Alert("Major investors abandon " + name, Alert.BAD_STOCK);
                channel.push(alert);
            }
            case 3 -> {
                // vol decrease
                target_vol /= 1.0 + (2.0 * rand.nextDouble()); // Random value between 1.0 and 3.0
                target_vol = Math.max(0.0, Math.min(5, target_vol)); // cap
                Alert alert = new Alert("Analysts expect calm year for " + name, Alert.GOOD_STOCK);
                channel.push(alert);
            }
            case 4 -> {
                // vol increase
                target_vol *= 1.0 + (3.0 * rand.nextDouble()); // Random value between 1.0 and 3.0
                target_vol = Math.max(0.0, Math.min(5, target_vol)); // cap
                Alert alert = new Alert(name + " under high watch this season", Alert.ALERT_STOCK);
                channel.push(alert);
            }
            case 5 -> {
                // risk change
                if (risk == 1) { risk++; }
                else { risk += (rand.nextInt() % 2 == 0) ? -1 : 1; }
                Alert alert = new Alert(name + " under new management", Alert.ALERT_STOCK);
                channel.push(alert);
            }
        }
    }
    // old behavior for stocks
    private void arcadeBehavior() {
        Random rand = new Random();

        double percentChange = ((rand.nextDouble() * 10) * vol); // 1. Random noise
        percentChange *= (rand.nextInt() % 2 == 0) ? -1 : 1;     // 2. Determine up or down
        percentChange += trend;                                  // 3. Incorporate overall trend
        value += (value * (percentChange / 100));                // 4. Apply change
        value = round(value);                                    // 5. Round to dollar value

        if (rand.nextInt(100) < 5) {
            trend += ((rand.nextDouble() - 0.5) * 10);  // Major trend fluctuation
        } else {
            trend += ((rand.nextDouble() - 0.5) * 2);   // Minor trend fluctuation
        }
        trend += (target_value - value) * 0.02;         // Adjust trend toward target_value
        trend = Math.max(-35, Math.min(trend, 35)); // cap trend
        // Adjust vol
        if (rand.nextInt(100) < 5) {
            vol += ((rand.nextDouble() - 0.5) * 2);  // Major fluctuation
        } else {
            vol += ((rand.nextDouble() - 0.5) * 0.5); // Minor fluctuation
        }
        vol += (target_vol - vol) * 0.05;  // Smooth adjustment towards target_vol
        vol = Math.max(0, Math.min(vol, 5));

        // handle random events
        if (rand.nextInt(50) < risk) {
            // currently very similar to randomEvent() but will see change
            int outcome = rand.nextInt(5) + 1;

            // equal likelihood of all events (for now)
            switch (outcome) {
                case 1 -> {
                    // stock boom
                    target_value *= 2.0 + (3.0 * rand.nextDouble()); // Random value between 2.0 and 5.0
                    target_value = Math.max(10.0, Math.min(1000000, target_value)); // cap
                    Alert alert = new Alert("Analysts predict strong rise for " + name, Alert.GOOD_STOCK);
                    channel.push(alert);
                }
                case 2 -> {
                    // stock crash
                    target_value /= 1.0 + (2.0 * rand.nextDouble()); // Random value between 1.0 and 3.0
                    target_value = Math.max(10.0, Math.min(1000000, target_value)); // cap
                    Alert alert = new Alert("Major investors abandon " + name, Alert.BAD_STOCK);
                    channel.push(alert);
                }
                case 3 -> {
                    // vol decrease
                    target_vol /= 1.0 + rand.nextDouble(); // Random value between 1.0 and 2.0
                    target_vol = Math.max(0.0, Math.min(5, target_vol)); // cap
                    Alert alert = new Alert("Analysts expect calm year for " + name, Alert.GOOD_STOCK);
                    channel.push(alert);
                }
                case 4 -> {
                    // vol increase
                    target_vol *= 2.0 + (3.0 * rand.nextDouble()); // Random value between 2.0 and 5.0
                    target_vol = Math.max(0.0, Math.min(5, target_vol)); // cap
                    Alert alert = new Alert(name + " under high watch this season", Alert.ALERT_STOCK);
                    channel.push(alert);
                }
                case 5 -> {
                    // risk change
                    if (risk == 0) { risk++; }
                    else { risk += (rand.nextInt() % 2 == 0) ? -1 : 1; }
                    Alert alert = new Alert(name + " under new management", Alert.ALERT_STOCK);
                    channel.push(alert);
                }
            }
        }
    }
    
    double getMax() {
        // get max value in history
        double maxVal = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < history.size(); i++) {
            double val = history.at(i);
            if (val > maxVal) {
                maxVal = val;
            }
        }
        if (value > maxVal) {
            // in case current is largest
            return value;
        }
        return maxVal;
    }
    double getMin() {
        // get min value in history
        double minVal = Double.POSITIVE_INFINITY;
        for (int i = 0; i < history.size(); i++) {
            double val = history.at(i);
            if (val < minVal) {
                minVal = val;
            }
        }
        if (value < minVal) {
            // in case current value is smallest
            return value;
        }
        return minVal;
    }

    // display functions 
    void printHistory() {
        final int ROWS = HISTORY_HEIGHT;       // max graph height
        final int COLS = HISTORY_LENGTH;       // max graph length
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
        // saves all info about the stock to a unique file
        try {
            // determine correct save path 
            String fileName;
            if (Main.NDEV) {
                String savePath = System.getenv("APPDATA") + "\\Terminal Trader\\gen\\";
                fileName = savePath + "s" + id + ".txt";
            }
            else {
                fileName = "gen/s" + id + ".txt";
            }

            PrintWriter writer = new PrintWriter(new FileWriter(fileName));

            writer.println("---STOCK-INFO---");
            writer.println(id);
            writer.println(name);

            // attributes
            writer.println("---ATTRIBUTES---");
            writer.println("DATA: target_value target_vol recordHigh recordLow trend vol risk");
            writer.print(target_value + " ");
            writer.print(target_vol + " ");
            writer.print(recordHigh + " ");
            writer.print(recordLow + " ");
            writer.print(trend + " ");
            writer.print(vol + " ");
            writer.print(risk + " ");
            writer.println(""); // newline

            // history
            writer.println("---HISTORY---");
            for (int i = history.size() - 1; i >= 0; i--) {
                // print in reverse order for when it loads
                writer.print(history.at(i) + " ");
            }
            writer.println("");
            writer.println("---INFO-END---");

            // label
            writer.println("For Terminal Trader, a text-based stock game.");
            writer.println("Developed and Designed by Samuel Johns");

            writer.close();
        } 
        catch (IOException e) {
            DB_LOG("IO Error: Stock Save Method -> " + e.getMessage());
        }
    }
    
    void load(int id) throws InitException {
        // determine correct save path
        String fileName;
        if (Main.NDEV) {
            String savePath = System.getenv("APPDATA") + "\\Terminal Trader\\gen\\";
            fileName = savePath + "s" + id + ".txt";
        }
        else {
            fileName = "gen/s" + id + ".txt";
        }

        File file = new File(fileName);
        try (Scanner scanner = new Scanner(file)) {
            this.id = id;
            // skip headers
            scanner.nextLine();
            scanner.nextLine();

            name = scanner.nextLine();

            // skip headers
            scanner.nextLine();
            scanner.nextLine();

            target_value = Double.parseDouble(scanner.next());
            target_vol = Double.parseDouble(scanner.next());
            recordHigh = Double.parseDouble(scanner.next());
            recordLow = Double.parseDouble(scanner.next());
            trend = Double.parseDouble(scanner.next());
            vol = Double.parseDouble(scanner.next());
            risk = Integer.parseInt(scanner.next());
            scanner.nextLine(); // consume newline char

            scanner.nextLine(); // skip header
            String token = scanner.next();
            while (!token.equals("---INFO-END---")) {
                history.push(Double.parseDouble(token));
                token = scanner.next();
            }

            if (history.size() == 0) {
                value = target_value; // init case
            }
            else {
                value = history.recent();
            }
        }
        catch (NoSuchElementException e) {
            throw new InitException("Corrupted Stock Data");
        }
        catch (FileNotFoundException e) {
            throw new InitException("File Not Found: " + fileName);
        }
    }
}