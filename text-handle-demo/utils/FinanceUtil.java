package com.dover.util;


import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author dover
 * @since 2021/3/19
 */
public class FinanceUtil {
    /**
     * 默认Double、Long、BigDecimal 保留两位小数
     */
    public static String toString(Number number) {
        if (number == null) {
            return "";
        }
        if (number instanceof Long || number instanceof Double) {
            return String.format("%.2f", number);
        }
        if (number instanceof BigDecimal) {
            return String.format("%.2f", ((BigDecimal) number).setScale(2, RoundingMode.HALF_UP));
        } else {
            return number.toString();
        }
    }
    /**
     * 默认Double、Long、BigDecimal 保留两位小数，并添加千分符
     */
    public static String toStringWithMark(Number number) {
        if (number == null) {
            return "";
        }
        if (number instanceof Long || number instanceof Double) {
            return String.format("%,.2f", number);
        }
        if (number instanceof BigDecimal) {
            return String.format("%,.2f", ((BigDecimal) number).setScale(2, RoundingMode.HALF_UP));
        } else {
            return number.toString();
        }
    }

    public static String trim(String money) {
        return StringUtils.isBlank(money) ? "0.00" : new BigDecimal(money).setScale(2, RoundingMode.HALF_UP).toString();
    }

    public static String fen2Yuan(String fen) {
        if (StringUtils.isBlank(fen)) {
            return "0";
        }
        return new BigDecimal(fen).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).toString();
    }

    public static BigDecimal divide(Number divided, Number divisor) {
        if (divided == null || divisor == null || divisor.doubleValue() == BigDecimal.ZERO.doubleValue()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(divided.doubleValue())
            .divide(BigDecimal.valueOf(divisor.doubleValue()), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal divide(Number divided, Number divisor, int scale) {
        if (divided == null || divisor == null || divisor.doubleValue() == BigDecimal.ZERO.doubleValue()) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(divided.doubleValue())
            .divide(BigDecimal.valueOf(divisor.doubleValue()), scale, RoundingMode.HALF_UP);
    }

    public static String calcPercent(Number divided, Number divisor) {
        return divide(BigDecimal.valueOf(divided.doubleValue()).multiply(BigDecimal.valueOf(100D)), divisor) + "%";
    }
}
