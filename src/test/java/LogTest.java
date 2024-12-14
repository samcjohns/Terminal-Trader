import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import tetrad.Log;

public class LogTest {
    @SuppressWarnings("deprecation")
    @Test
    public void testOrder() {
        Log<Double> log = new Log<>(10);

        log.push(1.0);
        log.push(2.0);
        log.push(3.0);

        assertEquals(3.0, log.recent());
        assertEquals(3.0, log.at(0));
        assertEquals(2.0, log.at(1));
        assertEquals(1.0, log.at(2));

        for (int i = 10; i <= 20; i++) {
            log.push(0.0 + i);
        }

        assertEquals(20.0, log.recent());
        assertEquals(20.0, log.at(0));
        assertEquals(19.0, log.at(1));
        assertEquals(18.0, log.at(2));
    }
}
