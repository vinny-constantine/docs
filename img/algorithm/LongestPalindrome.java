package com.dover.pdf;

/**
 * 查找最长回文子串
 *
 * @author dover
 * @since 2023/2/13
 */
public class LongestPalindrome {

    public static void main(String[] args) {
        System.out.println(longestPalindrome("aaaa"));
    }

    public static String longestPalindrome(String s) {
        char[] arr = s.toCharArray();
        // d[i][j]：表示从i ~ j的子串是否为回文串
        // 若d[i][j] 是回文串，则 d[i+1][j-1]是回文串 且 arr[i] == arr[j]，或者 i == j，或者 j = i+1 且 arr[i] == arr[j]
        // 状态转移方程为：d[i][j] = d[i+1][j-1] && arr[i] == arr[j]
        int[][] d = new int[arr.length][arr.length];
        int maxLen = 0;
        int resIdx = 0;
        for (int i = 0; i < arr.length; i++) {
            d[i][i] = 1;
        }
        for (int k = 1; k <= arr.length; k++) {
            for (int i = 0, j = i + k; j < arr.length; j++, i++) {
                if (arr[i] == arr[j]) {
                    if (j == i + 1) {
                        d[i][j] = 1;
                    } else if (i + 1 < arr.length && j - 1 >= 0 && d[i + 1][j - 1] == 1) {
                        d[i][j] = 1;
                    }
                }
                if (d[i][j] == 1 && j - i + 1 > maxLen) {
                    maxLen = j - i + 1;
                    resIdx = i;
                }
            }
        }
        return s.substring(resIdx, resIdx + maxLen);
    }
}
