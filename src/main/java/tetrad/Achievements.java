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
import static tetrad.Mutil.bold;
import static tetrad.Mutil.center;
import static tetrad.Mutil.green;
import static tetrad.Mutil.magenta;
import static tetrad.Mutil.red;

public class Achievements {
    boolean[] acvList;         // where achievement status is stored
    private User relevantUser; // who this belongs to
    private News channel;      // reference to News object for pushing Alerts

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

    Achievements(User relevantUser, News channel) {
        this.relevantUser = relevantUser;
        this.channel = channel;
        acvList = new boolean[ACV_AMOUNT];
    }

    void buyCheck() {
        if (!acvList[BUY_FIRST_STOCK]) {
            earnAcv(BUY_FIRST_STOCK);
        }
        if (!acvList[CASH_LT_DOLLAR] && relevantUser.getCash() < 1) {
            earnAcv(CASH_LT_DOLLAR);
        }
    }
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

    // prints full page of Achievements
    void printPage() {
        System.out.println(magenta(center("Achievements", MENU_WIDTH, "~")));
        System.out.println(""); // spacing
        System.out.println(green("Completed"));
        System.out.println(red("Uncompleted"));
        System.out.println(""); // spacing

        // print all achievments
        for(int i = 0; i < ACV_AMOUNT; i++) {
            if (acvList[i]) {
                System.out.println(bold(green(getAcvTitle(i))));
                System.out.println(green(getAvcDesc(i)));
            }
            else {
                System.out.println(bold(red(getAcvTitle(i))));
                System.out.println(red(getAvcDesc(i)));
            }
            System.out.println(""); // spacing
        }
    }

    // sets an achievement as earned and pushes the alert to the news
    private void earnAcv(int acv) {
        acvList[acv] = true;
        String msg = "Achievement Unlocked: ";
        msg += getAcvTitle(acv);
        Alert acvAlert = new Alert(msg, Alert.ACHIEVEMENT);
        channel.push(acvAlert);
    }
    // title for the given achievement
    private String getAcvTitle(int acv) {
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
    private String getAvcDesc(int acv) {
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
            case ADVANCE_1000_TIMES -> "Advance 1000 times"; // FIXME swapped
            default -> "Sam made a mistake"; // programmer error
        };
    }

    // have a unique save and load method
    void save() {
        try {
            String fileName;
            if (Main.NDEV) {
                String savePath = System.getenv("APPDATA") + "\\Terminal Trader\\saves\\";
                fileName = savePath + relevantUser.getName() + "_a.txt";
            }
            else {
                fileName = "saves/" + relevantUser.getName() + "_a.txt";
            }

            // saves all info about the stock to a unique file
            PrintWriter writer = new PrintWriter(new FileWriter(fileName));

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
    void load() throws InitException {
        String fileName;
        if (Main.NDEV) {
            String savePath = System.getenv("APPDATA") + "\\Terminal Trader\\saves\\";
            fileName = savePath + relevantUser.getName() + "_a.txt";
        }
        else {
            fileName = "saves/" + relevantUser.getName() + "_a.txt";
        }

        File file = new File(fileName);
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