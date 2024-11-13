package tetrad;

import java.util.LinkedList;

public class Log<T> {
    private final int maxSize; // Maximum size of the log
    private final LinkedList<T> log; // Data structure to hold the elements

    public static final int DEFAULT_SIZE = 100;

    // Constructor to initialize the log with a fixed size
    public Log(int maxSize) {
        this.maxSize = maxSize;
        this.log = new LinkedList<>();
    }

    // Add a new item to the front
    public void push(T item) {
        // Add the new value to the front
        log.addFirst(item);
        
        // Remove the oldest value if the log exceeds maxSize
        if (log.size() > maxSize) {
            log.removeLast();
        }
    }

    // Get the most recent value
    public T recent() {
        if (log.isEmpty()) {
            throw new IllegalStateException("Log is empty.");
        }
        return log.getFirst();
    }

    // Get the ith most recent value
    public T at(int i) {
        if (i < 0 || i >= log.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + i);
        }
        return log.get(i);
    }

    // Get the current size of the log
    public int size() {
        return log.size();
    }
}
