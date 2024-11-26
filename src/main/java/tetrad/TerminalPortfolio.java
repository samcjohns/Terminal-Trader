package tetrad;

import static tetrad.Mutil.HISTORY_HEIGHT;
import static tetrad.Mutil.dollar;

public class TerminalPortfolio extends Portfolio {
    TerminalPortfolio(User user) {
        super(user);
    }

    /**
     * Prints all transactions to the terminal
     */
    void printTransactionLogs() {
        for (int i = 0; i < transactions.size(); i++) {
            System.out.println(""); // spacing
            System.out.println("Stock: " + transactions.at(i).getStock().getName());
            System.out.println("Amount: " + transactions.at(i).getAmount());
            System.out.println("At: " + dollar(transactions.at(i).getPrice()));
        }
    }
    
    /**
     * Prints a graph displaying the history of the portfolio valuation
     */
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
}