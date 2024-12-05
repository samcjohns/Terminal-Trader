package tetrad;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static tetrad.Mutil.DB_LOG;

/**
 * A class that belongs to User that manages their achievements
 * 
 * @author Samuel Johns
 * Created: November 15, 2024
 * 
 * Description: The Achievement class is designed to manage the achievements
 *              of the user during gameplay. It handles checking for
 *              achievements, saving and loading a dedicated achievements file, 
 *              and managing awards that the User gains from each achievement.
 * 
 * @see User
 */

/* Titles, Descriptions, and Awards for Achievements
* 0. Day One 
*      - "Advance for the first time"
* 
* 1. First Step 
*      - "Buy your first share"
* 
* 2. ...And Done 
*      - "Sell your first share"
* 
* 3. Prodigy 
*      - "Double your initial investment"
* 
* 4. Quit Your Day Job 
*      - "Have 10 thousand in cash"
* 
* 5. Going Pro 
*      - "Have 100 thousand in cash"
* 
* 6. Hall of Fame 
*      - "Have 1 million in cash"
* 
* 7. Extraterrestrial 
*      - "Have 1 billion in cash"
* 
* 8. All In 
*      - "Hold less than a dollar in cash"
* 
* 9. Watcher 
*      - "Advance the market while holding no stocks"
* 
* 10. Veteran
*      - "Advance 100 times"
*
* 11. Mr. Omega
*      - "Advance 1000 times"
* 
* 12. Tithe Money (hidden)
*      - "Buy a stock on Sunday"
*
* 13. For The Love Of Money (hidden)
*      - "Buy a stock on Valentines Day"
*
* 14. Financial Independence (hidden)
*      - "Buy a stock on Independance Day"
*
* 15. Risen Indeed! (hidden)
*      - "Sell a stock on Easter"
*
* 16. Year-Round (hidden)
       - "Advance a whole year"
*/

public class Achievements {
    boolean[] acvList;               // where achievement status is stored
    private final User relevantUser; // who this belongs to
    private final Game game;         // reference to game object for Alerts and date

    // statics
    static final int ACV_AMOUNT = 18;

    // index for given achievements
    static final int       FIRST_ADVANCE = 0;
    static final int     BUY_FIRST_STOCK = 1;
    static final int    SELL_FIRST_STOCK = 2;
    static final int          DOUBLE_NET = 3;
    static final int            GAIN_10K = 4;
    static final int           GAIN_100K = 5;
    static final int             GAIN_1M = 6;
    static final int             GAIN_1B = 7;
    static final int             GAIN_1T = 8;
    static final int      CASH_LT_DOLLAR = 9;
    static final int ADVANCE_W_NO_STOCKS = 10;
    static final int   ADVANCE_100_TIMES = 11;
    static final int  ADVANCE_1000_TIMES = 12;
    static final int          SUNDAY_BUY = 13;
    static final int      VALENTINES_BUY = 14;
    static final int    INDEPENDENCE_BUY = 15;
    static final int         EASTER_SELL = 16;
    static final int           FULL_YEAR = 17;

    Achievements(User relevantUser, Game game) {
        this.relevantUser = relevantUser;
        this.game = game;
        acvList = new boolean[ACV_AMOUNT];
    }

    /** Checks for any achievements gained after a stock purchase */
    void buyCheck() {
        if (!acvList[BUY_FIRST_STOCK]) {
            earnAcv(BUY_FIRST_STOCK);
        }
        if (!acvList[CASH_LT_DOLLAR] && relevantUser.getCash() < 1) {
            earnAcv(CASH_LT_DOLLAR);
        }
        if (!acvList[SUNDAY_BUY] && game.cldr.getDayOfWeek().equals("SUNDAY")) {
            earnAcv(SUNDAY_BUY);
        }
        if (game.cldr.isHoliday(game.cldr.getToday())) {
            if (!acvList[VALENTINES_BUY] && game.cldr.holidayGreeting(game.cldr.getToday()).equals("Happy Valentine's Day")) {
                earnAcv(VALENTINES_BUY);
            }
            if (!acvList[INDEPENDENCE_BUY] && game.cldr.holidayGreeting(game.cldr.getToday()).equals("Happy Independence Day")) {
                earnAcv(INDEPENDENCE_BUY);
            }
        }
    }
    /** Checks for any achievements gained after a stock sale */
    void sellCheck() {
        if (!acvList[SELL_FIRST_STOCK]) {
            earnAcv(SELL_FIRST_STOCK);
        }
        if (!acvList[DOUBLE_NET] && relevantUser.getCash() >= User.STARTING_CASH * 2) {
            earnAcv(DOUBLE_NET);
        }
        if (!acvList[GAIN_10K] && relevantUser.getCash() >= 10000.0) {
            earnAcv(GAIN_10K);
        }
        if (!acvList[GAIN_100K] && relevantUser.getCash() >= 100000.0) {
            earnAcv(GAIN_100K);
        }
        if (!acvList[GAIN_1M] && relevantUser.getCash() >= 1000000.0) {
            earnAcv(GAIN_1M);
        }
        if (!acvList[GAIN_1B] && relevantUser.getCash() >= 1000000000.0) {
            earnAcv(GAIN_1B);
        }
        if (!acvList[GAIN_1T] && relevantUser.getCash() >= 1000000000000.0) {
            earnAcv(GAIN_1T);
        }
        if (game.cldr.isHoliday(game.cldr.getToday())) {
            if (!acvList[EASTER_SELL] && game.cldr.holidayGreeting(game.cldr.getToday()).equals("Happy Easter! He is risen!")) {
                earnAcv(EASTER_SELL);
            }
        }
    }
    /** Checks for any achievements gained after an advance */
    void advanceCheck() {
        if (!acvList[FIRST_ADVANCE]) {
            earnAcv(FIRST_ADVANCE);
        }
        if (!acvList[ADVANCE_W_NO_STOCKS] && relevantUser.getPortfolio().isEmpty()) {
            earnAcv(ADVANCE_W_NO_STOCKS);
        }
        if (!acvList[ADVANCE_100_TIMES] && relevantUser.getAdvances() >= 100) {
            earnAcv(ADVANCE_100_TIMES);
        }
        if (!acvList[ADVANCE_1000_TIMES] && relevantUser.getAdvances() >= 1000) {
            earnAcv(ADVANCE_1000_TIMES);
        }
        if (!acvList[FULL_YEAR] && relevantUser.getAdvances() >= 365) {
            earnAcv(FULL_YEAR);
        }
    }

    /* Prints full page of achievements
     * * This responsibility will soon be given to TerminalGame
     */
    @Deprecated
    void printPage() {
        
    }

    // Sets an achievement as earned and pushes an Alert to the News class
    private void earnAcv(int acv) {
        acvList[acv] = true;
        String msg = "Achievement Unlocked: ";
        msg += getAcvTitle(acv);
        Alert acvAlert = new Alert(msg, Alert.ACHIEVEMENT, game.cldr.getToday());
        game.news.push(acvAlert);
    }
    // title for the given achievement
    protected String getAcvTitle(int acv) {
        return switch (acv) {
            case FIRST_ADVANCE -> "Day One";
            case BUY_FIRST_STOCK -> "First Step";
            case SELL_FIRST_STOCK -> "...And Done";
            case DOUBLE_NET -> "Prodigy";
            case GAIN_10K -> "Quit Your Day Job";
            case GAIN_100K -> "Going Pro";
            case GAIN_1M -> "Hall of Fame";
            case GAIN_1B -> "Extraterrestrial";
            case GAIN_1T -> "Divine";
            case CASH_LT_DOLLAR -> "All In";
            case ADVANCE_W_NO_STOCKS -> "Watcher";
            case ADVANCE_100_TIMES -> "Veteran";
            case ADVANCE_1000_TIMES -> "Mr. Omega";
            case SUNDAY_BUY -> "Tithe Money";
            case VALENTINES_BUY -> "For The Love Of Money";
            case INDEPENDENCE_BUY -> "Financial Independence";
            case EASTER_SELL -> "Risen Indeed!";
            case FULL_YEAR -> "Year-Round";
            default -> "Sam made a mistake"; // programmer error
        };
    }
    // description for the given achievement (accounts for hidden ones)
    protected String getAvcDesc(int acv) {
        boolean show = acvList[acv];
        return switch (acv) {
            case FIRST_ADVANCE -> "Advance for the first time";
            case BUY_FIRST_STOCK -> "Buy your first share";
            case SELL_FIRST_STOCK -> "Sell your first share";
            case DOUBLE_NET -> "Double your initial investment";
            case GAIN_10K -> "Have 10 thousand in cash";
            case GAIN_100K -> "Have 100 thousand in cash";
            case GAIN_1M -> "Have 1 million in cash";
            case GAIN_1B -> "Have 1 billion in cash";
            case GAIN_1T -> "Have 1 trillion in cash";
            case CASH_LT_DOLLAR -> "Hold less than a dollar in cash";
            case ADVANCE_W_NO_STOCKS -> "Advance while holding no stocks";
            case ADVANCE_100_TIMES -> "Advance 100 times";
            case ADVANCE_1000_TIMES -> "Advance 1000 times";
            case SUNDAY_BUY -> show ? "Buy a stock on Sunday" : "Hidden";
            case VALENTINES_BUY -> show ? "Buy a stock on Valentine's Day" : "Hidden";
            case INDEPENDENCE_BUY -> show ? "Buy a stock on Independence Day" : "Hidden";
            case EASTER_SELL -> show ? "Sell a stock on Easter" : "Hidden";
            case FULL_YEAR -> show ? "Advance a whole year" : "Hidden";
            default -> "Sam made a mistake"; // programmer error
        };
    }

    /** Saves the user's achievement data to a unique file */
    void save() {
        try {
            String filePath = Main.getSource("saves");
            filePath += relevantUser.getName() + "_acv.txt";

            // saves all info about the stock to a unique file
            PrintWriter writer = new PrintWriter(new FileWriter(filePath));

            writer.println("---ACHIEVEMENT-INFO---");
            writer.println(relevantUser.getName());

            // status of achievements
            writer.println("---STATUS---");
            for (int i = 0; i < ACV_AMOUNT; i++) {
                writer.print(acvList[i] + " ");
            }
            writer.println(""); // newline
            writer.println("---INFO-END---");

            // label
            writer.println("For Terminal Trader, a text-based stock game.");
            writer.println("Developed and Designed by Samuel Johns");

            writer.close();
        } 
        catch (IOException e) {
            DB_LOG("ACV save(): " + e.getMessage());
        }
    }
    /** Loads the user's achievements from file 
     * @throws InitException if the file is not found or is corrupted/improper
     */
    void load() throws InitException {
        String filePath = Main.getSource("saves");
        filePath += relevantUser.getName() + "_acv.txt";
        File file = new File(filePath);
        try (Scanner scanner = new Scanner(file)) {
            // skip headers
            scanner.nextLine();
            scanner.nextLine(); // skip username
            scanner.nextLine(); 

            // get status
            for (int i = 0; i < ACV_AMOUNT; i++) {
                acvList[i] = scanner.nextBoolean();
            }
        }
        catch (NoSuchElementException e) {
            throw new InitException("Corrupted Achievement Data");
        }
        catch (FileNotFoundException e) {
            throw new InitException("File Not Found: " + file);
        }
    }
}