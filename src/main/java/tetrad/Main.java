package tetrad;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static tetrad.Mutil.pause;

public class Main {    
    static boolean NDEV = false; // false when testing in VSCode
    static boolean INIT = false; // true for first start only
    public static void main(String[] args) {
        if (args.length > 0) {
            NDEV = args[0].equals("-NDEV");
            if (args.length > 1) {
                INIT = args[1].equals("-INIT");
            }
        }

        boolean exit = false;

        // DEBUG: edit me
        boolean DEBUG = false;
        int DEBUG_MODE = 8;

        Scanner scanner = getSource(DEBUG, DEBUG_MODE);
        
        // outer loop: main menu
        // inner loop: in-game
        try {
            while (!exit) {
                Game game = new Game();
                exit = !game.startGame(scanner);
                boolean back = false;
                while (!back && !exit) {
                    try {
                        back = game.doTurn(scanner);
                    } 
                    catch (NumberFormatException e) {
                        System.out.println("Invalid Input");
                        pause(1000);
                    }
                }
                game.endGame();
            }
            scanner.close();
        }
        catch (NoSuchElementException e) {
            // DEBUG
            scanner.close();
            while(true) {} //suspend prog
        }
    }

    private static Scanner getSource(boolean DEBUG, int MODE) {
        if (DEBUG) {
            // determine test input file
            String fileName;
            switch (MODE) {
                case 1 -> fileName = "view_stock_test";
                case 2 -> fileName = "view_portfolio";
                case 3 -> fileName = "view_market";
                case 4 -> fileName = "repair_dependencies";
                case 5 -> fileName = "view_new_market";
                case 6 -> fileName = "new_save_plus";
                case 7 -> fileName = "load_new_save";
                case 8 -> fileName = "stock_walk";

                default -> fileName = "exit";
            }

            // open scanner and return
            try {
                fileName = "DEBUG/" + fileName + ".in";
                return new Scanner(new File(fileName));   
            }
            catch (FileNotFoundException e) {
                System.out.println("File Not Found");
                return null;
            }
        }
        else {
            return new Scanner(System.in);
        }
    }
}
