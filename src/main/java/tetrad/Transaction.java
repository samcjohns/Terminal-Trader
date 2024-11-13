package tetrad;

public class Transaction {
    private Stock stock;
    private int amount;
    private double price;

    public Transaction(Stock stock, int amount, double price) {
        this.stock = stock;
        this.amount = amount;
        this.price = price;
    }

    Stock  getStock() { return stock; }
    int   getAmount() { return amount; }
    double getPrice() { return price; }
}
