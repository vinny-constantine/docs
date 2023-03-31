package com.dover.pdf;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * @author dover
 * @since 2022/7/27
 */
public class FractionAddition {

    public static void main(String[] args) {
        String s = new FractionAddition().fractionAddition("7/2+2/3-3/4");
        System.out.println(s);
    }

    public String fractionAddition(String expression) {
        // 解析字符串构造分数列表
        LinkedList<Fraction> fractions = parse(expression);
        System.out.println(fractions);
        // 通分
        int commonDenominator = unifyDenominator(fractions);
        System.out.println(fractions);
        // 求和
        Fraction result = new Fraction(1, 0, commonDenominator);
        for (Fraction fraction : fractions) {
            result.numerator += fraction.symbol * fraction.numerator;
        }
        result.numerator = result.numerator < 0 ? result.numerator * (result.symbol = -1) : result.numerator;
        System.out.println(result);
        // 约分
        simplify(result);
        System.out.println(result);
        return (result.symbol == -1 ? "-" : "") + result.numerator + '/' + result.denominator;
    }

    public void simplify(Fraction fraction) {
        if (fraction.numerator == 0) {
            fraction.denominator = 1;
            return;
        }
        int divisor = Math.min(fraction.numerator, fraction.denominator);
        int devided = Math.max(fraction.numerator, fraction.denominator);
        while (devided % divisor != 0) {
            int tmp = divisor;
            divisor = devided % divisor;
            devided = tmp;
        }
        fraction.numerator = fraction.numerator / divisor;
        fraction.denominator = fraction.denominator / divisor;
    }

    public int unifyDenominator(LinkedList<Fraction> fractionList) {
        HashSet<Integer> set = new HashSet<>();
        for (Fraction fraction : fractionList) {
            set.add(fraction.denominator);
        }
        int commonDenominator = 1;
        for (Integer integer : set) {
            commonDenominator *= integer;
        }
        for (Fraction fraction : fractionList) {
            fraction.numerator = commonDenominator / fraction.denominator * fraction.numerator;
            fraction.denominator = commonDenominator;
        }
        return commonDenominator;
    }

    private LinkedList<Fraction> parse(String expression) {
        // 解析表达式
        LinkedList<Fraction> fractions = new LinkedList<>();
        Fraction fraction = null;
        StringBuilder numerator = new StringBuilder();
        StringBuilder denominator = new StringBuilder();
        boolean flag = true;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '+' || c == '-' || ('0' <= c && c <= '9' && i == 0)) {
                // 完成分母拼接
                if (denominator.length() > 0) {
                    fraction.denominator = Integer.parseInt(denominator.toString());
                    denominator.delete(0, denominator.length());
                }
                // 初始化分数
                fraction = new Fraction();
                fractions.add(fraction);
                fraction.symbol = c == '-' ? -1 : 1;
                // 开始拼接分子
                flag = true;
                // 特殊情况，字符以正分数开头，并省略了 + 号
                if ('0' <= c) {
                    numerator.append(c);
                }
            } else if (c == '/') {
                // 完成分子拼接
                fraction.numerator = Integer.parseInt(numerator.toString());
                numerator.delete(0, numerator.length());
                // 开始拼接分母
                flag = false;
            } else {
                if (flag) {
                    numerator.append(c);
                } else {
                    denominator.append(c);
                    // 完成分母拼接
                    if (i == expression.length() - 1) {
                        fraction.denominator = Integer.parseInt(denominator.toString());
                    }
                }
            }
        }
        return fractions;
    }

    /**
     * 分数
     */
    public static class Fraction {
        /**
         * 符号
         */
        int symbol;
        /**
         * 分子
         */
        int numerator;
        /**
         * 分母
         */
        int denominator;

        public Fraction() {
        }

        public Fraction(int symbol, int numerator, int denominator) {
            this.symbol = symbol;
            this.numerator = numerator;
            this.denominator = denominator;
        }

        @Override
        public String toString() {
            return symbol + " * " + numerator + '/' + denominator;
        }
    }
}
