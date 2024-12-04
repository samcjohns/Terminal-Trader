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
*      - Blue header
* 
* 1. First Step 
*      - "Buy your first share"
*      - Green header
* 
* 2. ...And Done 
*      - "Sell your first share"
*      - Magenta Header
* 
* 3. Prodigy 
*      - "Double your initial investment"
*      - Cyan Header
* 
* 4. Quit Your Day Job 
*      - "Have 10 thousand in cash"
*      - Yellow Header
* 
* 5. Going Pro 
*      - "Have 100 thousand in cash"
*      - Yellow background header
* 
* 6. Hall of Fame 
*      - "Have 1 million in cash"
*      - Green background header
* 
* 7. Extraterrestrial 
*      - "Have 1 billion in cash"
*      - Red background header
* 
* 8. All In 
*      - "Hold less than a dollar in cash"
*      - Cyan Background Header
* 
* 9. Watcher 
*      - "Advance the market while holding no stocks"
*      - Red header
* 
* 10. Veteran
*      - "Advance 100 times"
*      - Magenta background header
* 
* 11. Mr. Omega
*      - "Advance 1000 times"
*      - Blue background header
*/

public class Achievements {
    boolean[] acvList;               // where achievement status is stored
    private final User relevantUser; // who this belongs to
    private final Game game;         // reference to game object for Alerts and date

    // statics
    static final int ACV_AMOUNT = 12;

    // index for given achievements
    static final int       FIRST_ADVANCE = 0;
    static final int     BUY_FIRST_STOCK = 1;
    static final int    SELL_FIRST_STOCK = 2;
    static final int          DOUBLE_NET = 3;
    static final int            GAIN_10K = 4;
    static final int           GAIN_100K = 5;
    static final int             GAIN_1M = 6;
    static final int             GAIN_1B = 7;
    static final int      CASH_LT_DOLLAR = 8;
    static final int ADVANCE_W_NO_STOCKS = 9;
    static final int   ADVANCE_100_TIMES = 10;
    static final int  ADVANCE_1000_TIMES = 11;

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
    }
    /** Checks for any achievements gained after a stock sale */
    void sellCheck() {
        if (!acvList[SELL_FIRST_STOCK]) {
            earnAcv(SELL_FIRST_STOCK);
        }
        if (!acvList[DOUBLE_NET] && relevantUser.getCash() >= User.STARTING_CASH * 2) {
            earnAcv(DOUBLE_NET);
        }
        if (!acvList[GAIN_10K] && relevantUser.getCash() >= 10000) {
            earnAcv(GAIN_10K);
        }
        if (!acvList[GAIN_100K] && relevantUser.getCash() >= 100000) {
            earnAcv(GAIN_100K);
        }
        if (!acvList[GAIN_1M] && relevantUser.getCash() >= 1000000) {
            earnAcv(GAIN_1M);
        }
        if (!acvList[GAIN_1B] && relevantUser.getCash() >= 1000000000) {
            earnAcv(GAIN_1B);
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
            case CASH_LT_DOLLAR -> "All In";
            case ADVANCE_W_NO_STOCKS -> "Watcher";
            case ADVANCE_100_TIMES -> "Veteran";
            case ADVANCE_1000_TIMES -> "Mr. Omega";
            default -> "Sam made a mistake"; // programmer error
        };
    }
    // description for the given achievement
    protected String getAvcDesc(int acv) {
        return switch (acv) {
            case FIRST_ADVANCE -> "Advance for the first time";
            case BUY_FIRST_STOCK -> "Buy your first share";
            case SELL_FIRST_STOCK -> "Sell your first share";
            case DOUBLE_NET -> "Double your initial investment";
            case GAIN_10K -> "Have 10 thousand in cash";
            case GAIN_100K -> "Have 100 thousand in cash";
            case GAIN_1M -> "Have 1 million in cash";
            case GAIN_1B -> "Have 1 billion in cash";
            case CASH_LT_DOLLAR -> "Hold less than a dollar in cash";
            case ADVANCE_W_NO_STOCKS -> "Advance while holding no stocks";
            case ADVANCE_100_TIMES -> "Advance 100 times";
            case ADVANCE_1000_TIMES -> "Advance 1000 times";
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