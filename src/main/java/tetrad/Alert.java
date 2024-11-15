package tetrad;

/**
 * A class that stores information about an event pushed to the news
 * 
 * @author Samuel Johns
 * Created: November 15, 2024
 *
 * Description: A Alert is an object that stores relevant information about an
 *              event that occurs for a user, market, stock, or other class.
 *              When an event occurs, a class can push an alert to the News
 *              object held by the Game instance. The class that creates the 
 *              alert will give it relevant information so that it can be
 *              displayed by the News class. Alerts have references to other 
 *              relevant classes such as the one that creates it. They also 
 *              have a type which signifies the kind of event and how it will
 *              be displaying by the News class. It also has an age attribute.
 *              to track how old the alert is.
 */

class Alert {
    private final String headline;     // headline of alert to be displayed
    private final int type;            // type of alert (priority)
    private final User  relevantUser;  // user it pertains to (if applicable)
    private final Stock relevantStock; // stock it pertains to (if applicable)
    private int age;                   // how old the alert is

    // type ID's ranked by importance
    final static int  ACHIEVEMENT = 0;
    final static int    BAD_STOCK = 1;
    final static int   GOOD_STOCK = 2;
    final static int  ALERT_STOCK = 3;
    final static int   BAD_MARKET = 4;
    final static int  GOOD_MARKET = 5;
    final static int ALERT_MARKET = 6;

    Alert(String headline, int type) {
        this.headline = headline;
        this.type = type;
        this.relevantUser = null;
        this.relevantStock = null;
        age = 0;
    }
    Alert(String headline, int type, User relevantUser) {
        this.headline = headline;
        this.type = type;
        this.relevantUser = relevantUser;
        this.relevantStock = null;
        age = 0;
    }
    Alert(String headline, int type, Stock relevantStock) {
        this.headline = headline;
        this.type = type;
        this.relevantUser = null;
        this.relevantStock = relevantStock;
        age = 0;
    }
    
    String     getHeadline() { return headline; }
    int            getType() { return type; }
    User   getRelevantUser() { return relevantUser; }
    Stock getRelevantStock() { return relevantStock; }
    int             getAge() { return age; }

    void       incrementAge() { age++; }
}
