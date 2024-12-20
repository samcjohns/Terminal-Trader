package tetrad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Specialized terminal output manipulator
 * 
 * @author Samuel Johns
 * Created: December 19, 2024
 * Description: A specialized class for simplifying game output and menu
 *              construction in the terminal. Each screen object keeps track
 *              of what line it is currently on, the appropriate dimensions
 *              of the menu, and manipulating text on the screen. It is also
 *              designed to throw runtime exceptions when the proper screen
 *              size is violated. 
 */
public class ScreenBuilder {
    /** total width of the menu on screen */
    public static final int MENU_WIDTH = 120;

    /** total number of lines on screen */
    public static final int MENU_HEIGHT = 36;

    /** current line that the cursor is on */
    private int line;

    /** true when a page has been created, 
     * false when printing to fresh terminal */
    private boolean isBare;

    /** printstream where text is printed to */
    PrintStream out;

    /** 
     * Constructor 
     * @param out PrintStream where text is printed to
     */
    public ScreenBuilder(PrintStream out) {
        this.out = out;
        this.isBare = true;
        line = 0;
    }

    /**
     * Gets the current line number on screen
     * @return the current line number on screen
     */
    public int getLine() {
        return line;
    }

    /**
     * Prints the string to the current line (does not move to next line)
     * @param str string to be printed
     */
    public void print(String str) {
        // check for exceptions
        if (str.contains("\n")) {
            throw new ScreenBuilderException("Invalid Sequence In Printed Line: " + str);
        }
        if (line > MENU_HEIGHT) {
            throw new ScreenBuilderException("Menu Height Exceeded With Line: " + str);
        }

        out.print(str);
    }

    /**
     * Prints the string to the current line and moves to the next line
     * @param str string to be printed
     */
    public void println(String str) {
        // check for exceptions
        if (str.contains("\n")) {
            throw new ScreenBuilderException("Invalid Sequence In Printed Line: " + str);
        }
        if (line > MENU_HEIGHT) {
            throw new ScreenBuilderException("Menu Height Exceeded With Line: " + str);
        }

        out.print(str);
        cursorDown(1);
        line++;
    }

    /**
     * Prints string at the desired line, does not move to next line
     * @param str string to be printed
     * @param gotoline given line to print at
     */
    public void printAt(String str, int gotoLine) {
        if (gotoLine > this.line) {
            cursorDown(gotoLine - this.line);
            this.line = gotoLine;
        }
        else if (gotoLine < this.line) {
            cursorUp(this.line - gotoLine);
            this.line = gotoLine;
        }
        out.print("\r"); // goto beginning of line
        out.print(str);
    }

    /**
     * Moves to the next line
     */
    public void next() {
        if (isBare) {
            throw new ScreenBuilderException("Cannot Operate on Bare Screen");
        }
        if (line >= MENU_HEIGHT) {
            throw new ScreenBuilderException("Menu Height Exceeded With Next");
        }
        cursorDown(1);
        line++;
    }

    /**
     * Goes to the given line
     */
    public void gotoLine(int gotoLine) {
        if (isBare) {
            throw new ScreenBuilderException("Cannot Operate on Bare Screen");
        }
        if (line >= MENU_HEIGHT) {
            throw new ScreenBuilderException("Menu Height Exceeded With GoToLine");
        }
        if (gotoLine > line) {
            cursorDown(gotoLine - line);
            line = gotoLine;
        }
        else if (gotoLine < line) {
            cursorUp(line - gotoLine);
            line = gotoLine;
        }
        out.print("\r"); // goto beginning of the line
    }

    /**
     * Clears the screen and scrollback buffer using ANSI Escape Code
     */
    public void clearScreen() {
        out.print("\033[H\033[2J"); // clear screen
        out.println("\033[3J");     // clear scrollback buffer
        out.flush();
        isBare = true;
    }

    /**
     * Creates a fresh page with proper dimensions for printing
     */
    public void freshPage() {
        clearScreen();
        for (int i = 0; i < MENU_HEIGHT; i++) {
            out.println();
        }
        isBare = false;
    }

    /**
     * Clears the current line, and does not move
     */
    public void clearLine() {
        out.print("\r" + " ".repeat(MENU_WIDTH) + "\r");
    }

    /**
     * Clears the current line and num - 1 lines above
     * @param num number of lines to be erased
     */
    public void eraseLines(int num) {
        if (isBare) {
            throw new ScreenBuilderException("Cannot Operate on Bare Screen");
        }
        for (int i = 0; i < num; i++) {
            clearLine();
            cursorUp(1);
        }
    }

    /**
     * Moves the cursor up using ANSI Escape Codes, if there is text in from of
     * the cursor when it prints, it will be overwritten. Does nothing if cursor
     * is at the top.
     * @param num number of lines up
     */
    public void cursorUp(int num) {
        if (isBare) {
            throw new ScreenBuilderException("Cannot Operate on Bare Screen");
        }
        if (line > 0 && !isBare) {
            out.print("\033[" + num + "A");
            line--;
        }
    }

    /**
     * Moves the cursor down using ANSI Escape Codes, if there is text in from
     * of the cursor when it prints, it will be overwritten. Does nothing for 
     * bare page, throws runtime for screen exceeded.
     * @param num number of lines down
     */
    public void cursorDown(int num) {
        if (isBare) {
            throw new ScreenBuilderException("Cannot Operate on Bare Screen");
        }
        if (line >= MENU_HEIGHT) {
            throw new ScreenBuilderException("Max Screen Height Exceeded");
        }
        if (line < MENU_HEIGHT && !isBare) {
            out.print("\033[" + num + "B");
            line++;
        }
    }

    /**
     * Moves the cursor right using ANSI Escape Codes, if there is text in from of
     * the cursor when it prints, it will be overwritten.
     * @param num number of characters right
     */
    public void cursorRight(int num) {
        out.print("\033[" + num + "C");
    }

    /**
     * Moves the cursor down using ANSI Escape Codes, if there is text in from of
     * the cursor when it prints, it will be overwritten.
     * @param num number of characters left
     */
    public void cursorLeft(int num) {
        out.print("\033[" + num + "D");
    }

    /**
     * Takes a String and prints it as a header at the top of the page
     * @param title String to be used for header
     */
    public void header(String title) {
        int oLine = line;
        gotoLine(0);
        println("-".repeat(MENU_WIDTH));
        println("~ ".repeat((MENU_WIDTH - title.length())/4) + title + " ~".repeat((MENU_WIDTH - title.length())/4) + " ");
        println("-".repeat(MENU_WIDTH));
        gotoLine(oLine);
    }

    /**
     * Takes the string array of commands and creates a command tray at the
     * bottom of the screen
     * @param commands an array of Strings that represent commands that the 
     *                 the user can use in the given menu. Ex. "Next - '/'"
     */
    public void commandTray(String[] commands) {
        int oLine = line;
        gotoLine(33);
        println("-".repeat(MENU_WIDTH));
        print(commands[0]);
        for (int i = 1; i < commands.length; i++) {
            print(" | " + commands[i]);
        }
        cursorDown(1);
        println("-".repeat(MENU_WIDTH));
        gotoLine(oLine);
    }

    /**
     * Prints the ASCII art designated by the file name of the art in the 
     * assets directory. Prints the image and returns the cursor to the top of
     * the page to allow for overwriting.
     * @param fileName name of the file in the assets directory
     */
    public void backgroundImage(String fileName) {
        String filePath = Main.getSource("assets");
        filePath += fileName;

        gotoLine(3);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String currentLine; // current printed line of image
            while ((currentLine = reader.readLine()) != null && line < MENU_HEIGHT) {
                println(Mutil.cyan(center(currentLine, MENU_WIDTH)));
            }
        }
        catch (IOException e) {
            throw new MissingAssetException(fileName);
        }
    }

    // static methods
    /** 
     * Formats the string in the center of a given width
     * @return a centered String to be printed on the console
     * @param string the message to be center
     * @param width the total width after centering
     * @param mark the char to by used to center the message
    */
    public static String center(String string, int width, String mark) {
        if (string.length() < width) {
           return (mark.repeat((width - string.length())/2) + string + mark.repeat((width - string.length())/2));
        } 
        else {
           return string; // string too big, just return the whole thing
        }
   }

    /**
    * Formats the string in the center of a given width
    * @param string the message to be centered
    * @param width the width of the String after centering
    * @return a centered String to be printed on the console
    */
    public static String center(String string, int width) {
        return center(string, width, " ");
    }

    /**
    * Formats the string in the center of the screen
    * @param string the message to be centered
    * @return a centered String to be printed on the console
    */
    public static String center(String string) {
        return center(string, MENU_WIDTH, " ");
    }
}