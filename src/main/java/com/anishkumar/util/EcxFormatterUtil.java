package com.anishkumar.util;

import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;

@Slf4j

public class EcxFormatterUtil {

    private EcxFormatterUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final String DESCIMAL_CONSTANT_TWO = "#,##0.00";
    private static final String DESCIMAL_CONSTANT_THREE = "#,##0.00#";
    private static final String DESCIMAL_CONSTANT_FOUR = "#,##0.00##";

    /* Print formatters */

    public static DecimalFormat getFormatterForPriceField(int priceDecimalPrecision) {
        if (ECXCommonConstants.DECIMAL_PRECISION_CODE_UPTO4DIGITS == priceDecimalPrecision) {
            return new DecimalFormat(DESCIMAL_CONSTANT_FOUR);
        } else {
            return new DecimalFormat(DESCIMAL_CONSTANT_TWO);
        }
    }

    public static String formatFields(String amount, String currCode, boolean priceFieldIndicator,
                                      Integer priceDecimalPrecision) {
        // to give space between value & currency sign lager than 2
        if (currCode != null && currCode.length() > 2) {
            currCode = currCode + " ";
        }
        logger.debug("amount : {}, currCode :{}", amount, currCode);
        String currency = "-";
        String decimalValue = "0";
        DecimalFormat formatter = new DecimalFormat(DESCIMAL_CONSTANT_TWO);
        formatter.setRoundingMode(RoundingMode.DOWN);

        if (currCode != null) {
            currency = setCurrencyBasedOncurrCode(amount, currCode, priceFieldIndicator, priceDecimalPrecision,
                    currency, formatter);
            return currency;

        } else {
            if (amount != null) {
                if (amount.indexOf('.') != -1)
                    decimalValue = amount.substring(amount.indexOf('.'), amount.length());
                currency = setCurrencyValue(amount, currCode, decimalValue);
            }
        }
        logger.debug("currency code : {}", currency);
        return currency;

    }

    private static String setCurrencyBasedOncurrCode(String amount, String currCode, boolean priceFieldIndicator,
                                                     Integer priceDecimalPrecision, String currency, DecimalFormat formatter) {
        if (amount != null) {
            if (Double.parseDouble(amount) >= 0) {
                if (priceFieldIndicator) {
                    currency = currCode + getFormatterForPriceField(priceDecimalPrecision.intValue())
                            .format(Double.parseDouble(amount));
                } else {
                    currency = currCode + formatter.format(Double.parseDouble(amount));
                }
            } else {
                if (priceFieldIndicator) {
                    currency = "- " + currCode + getFormatterForPriceField(priceDecimalPrecision.intValue())
                            .format(Math.abs(Double.parseDouble(amount)));
                } else {
                    currency = "- " + currCode + formatter.format(Math.abs(Double.parseDouble(amount)));
                }
            }
        }
        logger.debug("currency code : {}", currency);
        return currency;
    }

    private static String setCurrencyValue(String amount, String currCode, String decimalValue) {
        String currency;
        DecimalFormat formatter;
        if (decimalValue.length() > 4 && currCode == null) {
            formatter = new DecimalFormat(DESCIMAL_CONSTANT_FOUR);

        } else if (decimalValue.length() > 3 && currCode == null) {
            formatter = new DecimalFormat(DESCIMAL_CONSTANT_THREE);
        } else {
            formatter = new DecimalFormat(DESCIMAL_CONSTANT_TWO);
        }
        currency = formatter.format(Double.parseDouble(amount));
        logger.debug("currency code : {}", currency);
        return currency;
    }

    /* Export formatters */

    public static String nullCheck(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    public static String nullCheckForDouble(Double d, boolean currencyIndicator, String currencyCode,
                                            boolean percentIndicator, boolean priceFieldIndicator, Integer priceDecimalPrecision) {
        if (d == null) {
            return "";
        }

        if (currencyIndicator) {
            if (priceFieldIndicator && priceDecimalPrecision != null
                    && priceDecimalPrecision.intValue() == ECXCommonConstants.DECIMAL_PRECISION_CODE_UPTO4DIGITS) {
                return currencyCode + new DecimalFormat(DESCIMAL_CONSTANT_FOUR).format(d.doubleValue());
            }
            return currencyCode + new DecimalFormat(DESCIMAL_CONSTANT_TWO).format(d.doubleValue());
        } else if (percentIndicator) {
            if (priceFieldIndicator && priceDecimalPrecision != null
                    && priceDecimalPrecision.intValue() == ECXCommonConstants.DECIMAL_PRECISION_CODE_UPTO4DIGITS) {
                return new DecimalFormat(DESCIMAL_CONSTANT_FOUR).format(d.doubleValue()) + "%";
            }
            return new DecimalFormat(DESCIMAL_CONSTANT_TWO).format(d.doubleValue()) + "%";
        }

        else {
            return formatNumber(d);
        }
    }

    private static String formatNumber(Double val) {
        int lengthOfValAftrDec = val.toString().substring(val.toString().indexOf(".") + 1, val.toString().length())
                .length();
        if (lengthOfValAftrDec > 4 || lengthOfValAftrDec == 4) {
            return new DecimalFormat(DESCIMAL_CONSTANT_FOUR).format(val.doubleValue());
        } else if (lengthOfValAftrDec == 3) {
            return new DecimalFormat(DESCIMAL_CONSTANT_THREE).format(val.doubleValue());
        } else {
            return new DecimalFormat(DESCIMAL_CONSTANT_TWO).format(val.doubleValue());
        }
    }

    public static void spaceAftercurrency(int i, String text) {
        if (i > 3) {
            text += " ";
        }
    }

    public static String appendNegativeSign(String str) {
        if(str.contains("-")) {
            StringBuilder stringBuilder = new StringBuilder(str.replace("-", ""));
            char characterToAdd = '-';
            stringBuilder.insert(0, characterToAdd);
            return stringBuilder.toString();
        } else{
            return str;
        }
    }
}
