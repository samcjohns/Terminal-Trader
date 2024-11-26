package tetrad;

/**
 * Simple container for storing information about transactions, including the
 * relevent stock, the amount purchased or sold, and the price at which it was
 * sold or bought at.
 * 
 * @author Samuel Johns
 * Created: November 15, 2024
 */
public class Transaction {
    private Stock stock;
    private int amount;
    private double price;

    public Transaction(Stock stock, int amount, double price) {
        this.stock = stock;
        this.amount = amount;
        this.price = price;
    }

    /**
     * @return relevant stock in the transaction
     */
    Stock  getStock() { return stock; }

    /**
     * @return amount of stock purchased or sold (negative for a sale)
     */
    int   getAmount() { return amount; }

    /**
     * @return returns purchase price of the stock purchased
     */
    double getPrice() { return price; }
}
