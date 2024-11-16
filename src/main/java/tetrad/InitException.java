package tetrad;

/**
 * An exception for signifying that an error has occurred in the initialization
 * of the game. This includes loading errors 
 * 
 * @author Samuel Johns
 * Created: November 15, 2024
 */

public class InitException extends Exception {
    public InitException(String message) {
        super(message);
    }
}
