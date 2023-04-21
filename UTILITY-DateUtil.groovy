import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/**
 * README
 * This utility allows you to manage dates
 *
 * Name: DateUtil
 * Description: Manage dates (Convert and format)
 * Date       Changed By                     Description
 * 20230404   Ludovic Travers                Create Utility DateUtil
 */

public class DateUtil extends ExtendM3Utility {

  /**
   * Get date in yyyyMMdd format
   * @return date
   */
  public String currentDateY8AsString() {
    return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
  }

  /**
   * Get date in yyyyMMdd format
   * @return date
   */
  public int currentDateY8AsInt() {
    return currentDateY8AsString().toInteger()
  }

  /**
   * Get date in yyMMdd format
   * @return date
   */
  public String currentDateY6AsString() {
    return LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
  }

  /**
   * Get date in yyMMdd format
   * @return date
   */
  public int currentDateY6AsInt() {
    return Integer.valueOf(currentDateY6AsString());
  }

  /**
   * Get time in HHmmSS format
   * @return time
   */
  public String currentTimeAsString() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
  }

  /**
   * Get time in HHmmSS format
   * @return time
   */
  public int currentTimeAsInt() {
    return Integer.valueOf(currentTimeAsString());
  }

  /**
   * Get time as epoch milliseconds
   * @return epoch milliseconds
   */
  public long currentEpochMilliseconds() {
    return Instant.now().toEpochMilli();
  }

  /**
   * Get time as epoch milliseconds
   * @return epoch seconds
   */
  public long currentEpochSeconds() {
    return Instant.now().getEpochSecond();
  }

  /**
   * Get difference between two dates e.g. difference of days/months/years between two dates
   * @param d1 First date
   * @param d1Format First date format
   * @param d2 Second date
   * @param d2Format Second date format
   * @param unit Chronological unit to calculate the diff in between
   * @return Difference between two given dates
   */
  public long dateDiff(String d1, String d1Format, String d2, String d2Format, ChronoUnit unit) {
    LocalDate date1 = LocalDate.parse(d1, DateTimeFormatter.ofPattern(d1Format))
    LocalDate date2 = LocalDate.parse(d2, DateTimeFormatter.ofPattern(d2Format))
    return unit.between(date1, date2)
  }

  /**
   * Check if date is valid
   * @param date Date to check
   * @param format Format of the date
   * @return {@code true} if date is valid
   */
  public boolean isDateValid(String date, String format) {
    try {
      LocalDate.parse(date, DateTimeFormatter.ofPattern(format))
      return true
    } catch (DateTimeParseException e) {
      return false
    }
  }
}
