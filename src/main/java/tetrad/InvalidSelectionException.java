package tetrad;

/**
 * An exception for selection errors made by the user
 * 
 * @author Samuel Johns
 * Created: November 15, 2024
 */

public class InvalidSelectionException extends Exception {
    public InvalidSelectionException(String message) {
        super(message);
    }
}