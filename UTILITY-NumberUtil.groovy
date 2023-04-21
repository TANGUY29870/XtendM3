/**
 * README
 * This utility allows you to manage numbers
 *
 * Name: DateUtil
 * Description: Manage numbers (Convert and format)
 * Date       Changed By                     Description
 * 20230404   Ludovic Travers                Create Utility NumberUtil
 */
 
public class NumberUtil extends ExtendM3Utility {

  /**
   * Check if number is valid
   * @param number Number
   * @param decimalSeparator Decimal separator
   * @return {@code true} if number is a valid number
   */
  public boolean isValidNumber(String number, String decimalSeparator) {
    if (toDouble(number, decimalSeparator).isPresent()) {
      return true
    }
    return false
  }

  /**
   * Convert number to double
   * @param number Number
   * @param decimalSeparator Decimal separator
   * @return Optional double if number is valid, empty optional otherwise
   */
  public Optional<Double> toDouble(String number, String decimalSeparator) {
    try {
      return Optional.of(Double.valueOf(formatToStandardSeparator(number, decimalSeparator)))
    } catch (NumberFormatException e) {
      return Optional.empty()
    }
  }

  /**
   * Convert number to integer
   * @param number Number
   * @param decimalSeparator Decimal separator
   * @return Optional integer if number is valid, empty optional otherwise
   */
  public Optional<Integer> toInteger(String number, String decimalSeparator) {
    Optional<Double> d = toDouble(number, decimalSeparator)
    if (d.isPresent()) {
      return Optional.of(d.get().toInteger())
    }
    return Optional.empty();
  }

  /**
   * Format number to use standard decimal separator (.)
   * @param number Number
   * @param decimalSeparator Custom decimal separator
   * @return Number using standard decimal separator
   */
  public String formatToStandardSeparator(String number, String decimalSeparator) {
    if (decimalSeparator == null || decimalSeparator.trim().isBlank()) {
      decimalSeparator = "."
    }
    decimalSeparator = decimalSeparator.trim()
    return number.replace(decimalSeparator, ".")
  }

  /**
   * Get number of digits in a number
   * @param number Number
   * @return Number of digits
   */
  public Integer getNumberOfDigits(Number number) {
    BigDecimal bd = new BigDecimal(number.toString())
    return bd.signum() == 0 ? 1 : bd.precision() - bd.scale()
  }

  /**
   * Get number of digits in number string
   * @param number Number
   * @param decimalSeparator Decimal separator
   * @return Number of digits if given number is valid, null otherwise
   */
  public Integer getNumberOfDigits(String number, String decimalSeparator) {
    Optional<Double> d = toDouble(number, decimalSeparator)
    if (d.isPresent()) {
      return getNumberOfDigits(d.get())
    }
    return null
  }

  /**
   * Get number of decimals in a number
   * @param number Number
   * @return Number of decimals
   */
  public Integer getNumberOfDecimals(Number number) {
    BigDecimal bd = new BigDecimal(number.toString())
    return bd.scale()
  }

  /**
   * Get number of decimals in number string
   * @param number Number
   * @param decimalSeparator Decimal separator
   * @return Number of decimals if given number is valid, null otherwise
   */
  public Integer getNumberOfDecimals(String number, String decimalSeparator) {
    Optional<Double> d = toDouble(number, decimalSeparator)
    if (d.isPresent()) {
      return getNumberOfDecimals(d.get())
    }
    return null
  }
  
  /**
   *  Transform a string parameter into an integer
   * @return integer
   */
  Integer parseStringToInteger(String parameter) {
    String wParameter = parameter.trim()
    if (wParameter.isInteger()) return Integer.valueOf(wParameter)
    else return 0
  }

  /**
   *  Transform a string parameter into an double
   * @return double
   */
  Double parseStringToDouble(String parameter) {
    String wParameter = parameter.trim()
    if (wParameter.isDouble()) return Double.valueOf(wParameter)
    else return 0d
  }
}
