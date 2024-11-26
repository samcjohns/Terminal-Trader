package tetrad;

import java.util.LinkedList;

/**
 * Limited-size, log for storing item in order
 * 
 * @author Samuel Johns
 * Created: November 15, 2024
 * 
 * Description: Fixed-size, linked list generic for storing items in order.
 *              The Log has a maxSize, when the size is exceeded, the oldest
 *              is removed and the next item is added.
 */
public class Log<T> {
    private final int maxSize;       // maximum size of the log
    private final LinkedList<T> log; // data structure to hold the elements

    public static final int DEFAULT_SIZE = 100;

    // Constructor to initialize the log with a fixed size
    public Log(int maxSize) {
        this.maxSize = maxSize;
        this.log = new LinkedList<>();
    }

    /**
     * Adds a new item to the front of the log
     * @param item item to be pushed
     */
    public void push(T item) {
        // Add the new value to the front
        log.addFirst(item);
        
        // Remove the oldest value if the log exceeds maxSize
        if (log.size() > maxSize) {
            log.removeLast();
        }
    }

    /**
     * Gets the most recent item from the log
     * @return most recent item, T
     */
    public T recent() {
        if (log.isEmpty()) {
            throw new IllegalStateException("Log is empty.");
        }
        return log.getFirst();
    }

    /**
     * Returns the item at i
     * @param i index of the item to be returned
     * @return the item at index i
     */
    public T at(int i) {
        if (i < 0 || i >= log.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + i);
        }
        return log.get(i);
    }

    /**
     * Returns the size of the log
     * @return size of this Log
     */
    public int size() {
        return log.size();
    }
}
