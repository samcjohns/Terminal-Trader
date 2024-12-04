package tetrad;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

import static tetrad.Alert.HOLIDAY;

/**
 * Centralized Data Class, stores current date and can calculate differences.
 * 
 * @author Samuel Johns
 * Created: November 29, 2024
 * 
 * Description: The Calendar class is designed to store the current date and
 *              calculate the number of days between the current date and
 *              another date.
 */
public class Calendar {
        private LocalDate today;

        // reference to current game object for announcing holidays
        private Game game;
    
        /**
         * Contruct the calendar with a custom start date
         * @param startDate start date for calendar object
         */
        public Calendar(LocalDate startDate, Game game) {
            this.today = startDate;
            this.game = game;
        }

        /**
         * Defaults current date to right now
         * @param game current game object
         */
        public Calendar(Game game) {
            this.today = LocalDate.now();  // Use today's date
            this.game = game;
        }

    
        /**
         * Advances the current date
         * @param days number of days to advance
         */
        public void advance(int days) {
            today = today.plusDays(days);
            if (isHoliday(today)) {
                Alert alert = new Alert(holidayGreeting(today), HOLIDAY, today);
                game.news.push(alert);
            }
        }

        /**
         * Advances the current date by one day
         */
        public void advance() {
            this.advance(1);
        }
    
        /**
         * @return current date
         */
        public LocalDate getToday() {
            return today;
        }
    
        /**
         * Gets number of days between current date and given date
         * @param date given date
         * @return days between current and given date
         */
        public int daysBetween(LocalDate date) {
            return Math.abs((int) ChronoUnit.DAYS.between(date, today));
        }
    
        /** 
         * Get the date that corresponds to the given number of days from the current date 
         * @param days number of days from current date
         * @return LocalDate 'days' from current date
         */
        public LocalDate dateFromNow(int days) {
            return today.plusDays(days);
        }
    
        /**
         * Return current date as a formatted string
         * @return formated date
         */
        public String getFormattedToday() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            return today.format(formatter);
        }

        /**
         * Return date as a formatted string
         * @param date given date
         * @return formatted date
         */
        public String getFormattedDate(LocalDate date) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            return date.format(formatter);
        }

        /**
         * Checks if the current date is the first day of the month
         * @return true if today is the first day of the month, false otherwise
         */
        public boolean isFirstDayOfMonth() {
            return today.getDayOfMonth() == 1;
        }

        /**
         * Checks if a given date is the first day of the month
         * @param date given date
         * @return true if the given date is the first day of the month, false otherwise
         */
        public boolean isFirstDayOfMonth(LocalDate date) {
            return date.getDayOfMonth() == 1;
        }

        /**
         * Returns days in the current month
         * @return days in current month
         */
        public int daysInCurrentMonth() {
            return today.lengthOfMonth();
        }
        
        /**
         * Returns days in a given month
         * @param year given year
         * @param month given month
         * @return days in given month
         */
        public int daysInMonth(int year, int month) {
            return LocalDate.of(year, month, 1).lengthOfMonth();
        }
        
        /**
         * Returns day of the week as a String (e.g., "MONDAY")
         * @return day of the week as a String
         */
        public String getDayOfWeek() {
            return today.getDayOfWeek().toString();
        }
        
        /**
         * Returns the day of the week of a given date as a String 
         * (e.g., "FRIDAY")
         * @param date given date
         * @return day of the week as a String
         */
        public String getDayOfWeek(LocalDate date) {
            return date.getDayOfWeek().toString();
        }

        /**
         * Advance the calendar by months and return the number of days passed
         * @param months number of months to advance
         * @return number of days passed
         */
        public int advanceMonths(int months) {
            LocalDate oldDate = today;
            today = today.plusMonths(months);
            return (int) ChronoUnit.DAYS.between(oldDate, today);
        }

        /**
         * Advance the calendar by years and return the number of days passed
         * @param years number of years to advance
         * @return number of days passed
         */
        public int advanceYears(int years) {
            LocalDate oldDate = today;
            today = today.plusYears(years);
            return (int) ChronoUnit.DAYS.between(oldDate, today);
        }
        
        /**
         * @return true if current year is leap year
         */
        public boolean isLeapYear() {
            return today.isLeapYear();
        }
        
        /**
         * @param year given year
         * @return if given year is leap year
         */
        public boolean isLeapYear(int year) {
            return LocalDate.of(year, 1, 1).isLeapYear();
        }
        
        /**
         * @param date given date
         * @return if year of given date is leap year
         */
        public boolean isLeapYear(LocalDate date) {
            return date.isLeapYear();
        }

        /**
         * Returns true if given date is today
         * @param date given date
         * @return true if given date is today
         */
        public boolean isToday(LocalDate date) {
            return today.isEqual(date);
        }

        /**
         * Changes current date to given date
         * @param newDate new current date
         */
        public void resetToDate(LocalDate newDate) {
            this.today = newDate;
        }

        /**
         * Gets the week number of current day
         * @return week number of today
         */
        public int getWeekNumber() {
            return today.get(ChronoField.ALIGNED_WEEK_OF_YEAR);  // Week number in the year
        }        
        
        /**
         * Checks if given date is a holiday
         * @param date given date
         * @return true if given date is a holiday
         */
        public boolean isHoliday(LocalDate date) {
            int day = date.getDayOfMonth();
            Month month = date.getMonth();

            // New Year's Day - January 1
            if (month == Month.JANUARY && day == 1) {
                return true;
            }

            // New Year's Eve - December 31
            if (month == Month.DECEMBER && day == 31) {
                return true;
            }

            // Valentine's Day - February 14
            if (month == Month.FEBRUARY && day == 14) {
                return true;
            }

            // St. Patrick's Day - March 17
            if (month == Month.MARCH && day == 17) {
                return true;
            }

            // Easter (variable date)
            if (date.equals(getEasterSunday(date.getYear()))) {
                return true;
            }

            // Fourth of July - Independence Day
            if (month == Month.JULY && day == 4) {
                return true;
            }

            // Labor Day (first Monday in September)
            if (month == Month.SEPTEMBER && date.equals(date.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)))) {
                return true;
            }

            // Thanksgiving (fourth Thursday in November)
            if (month == Month.NOVEMBER && date.equals(date.with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY)))) {
                return true;
            }

            // Christmas Day - December 25
            if (month == Month.DECEMBER && day == 25) {
                return true;
            }

            // Add more custom holiday checks here as needed
            return false;
        }

        // returns date of Easter Sunday of the given date
        private LocalDate getEasterSunday(int year) {
            int a = year % 19;
            int b = year / 100;
            int c = year % 100;
            int d = b / 4;
            int e = b % 4;
            int f = (b + 8) / 25;
            int g = (b - f + 1) / 3;
            int h = (19 * a + b - d - g + 15) % 30;
            int i = c / 4;
            int k = c % 4;
            int l = (32 + 2 * e + 2 * i - h - k) % 7;
            int m = (a + 11 * h + 22 * l) / 451;
            int month = (h + l - 7 * m + 114) / 31;
            int day = ((h + l - 7 * m + 114) % 31) + 1;
            return LocalDate.of(year, month, day);
        }

        /**
         * Returns greeting for the holiday of a given date
         * @param date given date
         * @return String for the holiday greeting
         */
        public String holidayGreeting(LocalDate date) {
            int day = date.getDayOfMonth();
            Month month = date.getMonth();

            // New Year's Day - January 1
            if (month == Month.JANUARY && day == 1) {
                return "Happy New Year! " + today.getYear();
            }

            // New Year's Eve - December 31
            if (month == Month.DECEMBER && day == 31) {
                return "Happy New Year's Eve";
            }

            // Valentine's Day - February 14
            if (month == Month.FEBRUARY && day == 14) {
                return "Happy Valentine's Day";
            }

            // St. Patrick's Day - March 17
            if (month == Month.MARCH && day == 17) {
                return "Happy St. Patrick's Day";
            }

            // Easter (variable date)
            if (date.equals(getEasterSunday(date.getYear()))) {
                return "Happy Easter! He is risen!";
            }

            // Fourth of July - Independence Day
            if (month == Month.JULY && day == 4) {
                return "Happy Independence Day";
            }

            // Labor Day (first Monday in September)
            if (month == Month.SEPTEMBER && date.equals(date.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)))) {
                return "Happy Labor Day";
            }

            // Thanksgiving (fourth Thursday in November)
            if (month == Month.NOVEMBER && date.equals(date.with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY)))) {
                return "Happy Thanksgiving";
            }

            // Christmas Day - December 25
            if (month == Month.DECEMBER && day == 25) {
                return "Merry Christmas!";
            }

            // Add more custom holiday checks here as needed
            return null;
        }
}
