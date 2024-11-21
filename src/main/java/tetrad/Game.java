package tetrad;

import static tetrad.Mutil.MENU_WIDTH;
import static tetrad.Mutil.clearScreen;
import static tetrad.Mutil.dollar;
import static tetrad.Mutil.pause;
import static tetrad.Mutil.round;

@SuppressWarnings("unused")
public class Game {
    User usr;
    Market mkt;
    News news;

    static int headerSetting = -1; // color setting for the header
    static boolean ARCADE_MODE = false; // activates old stock behavior

    public Game() {
        news = new News();
        mkt = new Market(news);
        usr = new User(news, this);
    }

    // do nothing for generic Game class
    public void startGame() {}
    public void doTurn() {}
    public void endGame() {}

    void advance() {
        mkt.advance();
        usr.update();
        news.update();
    }
    void advance(int times) {
        for (int i = 0; i < times; i++) {
            this.advance();
        }
    }
    // specific advance for init case (only affects market values)
    protected void initAdvance() {
        mkt.advance();
    }
    
    // save functions
    public void saveGame() {
        mkt.save();
        usr.save();
    }
    public void loadGame(String username) throws InitException {
        mkt.load();
        usr.load(username, mkt);
    }
    protected void createSaveFile(String username) {
        // check dependencies
        try {
            mkt.load();
        }
        catch (InitException e) {
            createGen();
        }

        usr = new User(username, User.STARTING_CASH, news, this);
        usr.save();
    }

    // dev tools
    protected void createGen() {
        // builds save files for stocks and market
        mkt = new Market(news);

        // prep market
        mkt.setTrend(0); // for now FIXME
        mkt.setNumStocks(10);

        try {
            Stock s0 = new Stock(0, "Burley Buns", 18.24, 0.7, 2);
            Stock s1 = new Stock(1, "Logan Logs", 100.00, 1, 1);
            Stock s2 = new Stock(2, "Shorehaven", 17000.00, 0.5, 1);
            Stock s3 = new Stock(3, "GreenCard", 42.00, 0.9, 3);
            Stock s4 = new Stock(4, "MurphCo", 64.00, 1.1, 2);
            Stock s5 = new Stock(5, "Orion", 10.00, 1.4, 1);
            Stock s6 = new Stock(6, "Rinnova", 1200, 1.2, 1);
            Stock s7 = new Stock(7, "Beans & Cream", 56.00, 1.3, 1);
            Stock s8 = new Stock(8, "Mom & Dad's", 789.00, 0.6, 1);
            Stock s9 = new Stock(9, "The Rip", 420.00, 2.7, 3);

            // new!
            Stock s10 = new Stock(9, "Samuel's Johns", 420.00, 2.7, 3);
            Stock s11 = new Stock(9, "Jenna Gyms", 420.00, 2.7, 3);
            Stock s12 = new Stock(9, "Rockford Mine", 420.00, 2.7, 3);
            Stock s13 = new Stock(9, "Colin Call-Center", 420.00, 2.7, 3);
            Stock s14 = new Stock(9, "Dave's Diner", 420.00, 2.7, 3);

            s0.save();
            s1.save();
            s2.save();
            s3.save();
            s4.save();
            s5.save();
            s6.save();
            s7.save();
            s8.save();
            s9.save();
        } 
        catch (InitException e) {
            System.err.println("Exception In: createGen()");
            System.err.println("This: " + e.getMessage());
        }

        mkt.save();

        try { 
            mkt.load(); // mkt should look for s0-9 because i set numStocks to 10
        } 
        catch (InitException e) { 
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }

        // advance time to generate histories
        advance(100);

        // save files
        mkt.save();
        news.clear(); // clear acv caused by advancing
    }
    protected void stockWalk(int stockID, int times) {
        Stock stock = mkt.getStock(stockID);
        for (int i = 0; i < times; i++) {
            clearScreen();
            System.out.print(stock.getName() + " | ");
            System.out.print("At " + dollar(stock.getValue()) + " | ");
            System.out.print("Target Value: " + dollar(stock.getTargetVal()) + " | ");
            System.out.print("Trend: " + round(stock.getTrend()) + " | ");
            System.out.println("Vol: " + round(stock.getVol()));
            System.out.println("-".repeat(MENU_WIDTH));
            stock.printHistory();
            System.out.println("-".repeat(MENU_WIDTH));
            pause(100); // pause 1/10 of second
            initAdvance();
        }
    }
}