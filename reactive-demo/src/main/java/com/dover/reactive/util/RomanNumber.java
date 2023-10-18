package com.dover.reactivedemo.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author dover
 * @since 2023/8/21
 */
public class Factorization {

    /**
     * 查找因子
     */
    public static Collection<Integer> findfactor(int number) {
        List<Integer> factors = new ArrayList<>();
        for (int i = 1; i <= number; i++) {
            if (number % i == 0) {
                factors.add(i);
            }
        }
        return factors;
    }
}
