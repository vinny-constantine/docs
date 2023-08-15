package com.dover.pdf;

/**
 * @author dover
 * @since 2022/7/27
 */
public class LengthOfLongestSubstring {


    public static int lengthOfLongestSubstring(String s) {
        byte[] bytes = new byte[126];
        int[] pos = new int[126];
        int maxLength = 0;
        int length = 0;
        for (int i = 0; i < s.length(); i++) {
            int c = s.charAt(i);
            if (bytes[c] == 1) {
                maxLength = Math.max(maxLength, length);
                length =  i - pos[c] - 1;
                for (int j = 0; j < i; j++) {
                    bytes[s.charAt(j)] = (byte) (j > pos[c] ? 1 : 0);
                }
            }
            length++;
            bytes[c] = 1;
            pos[c] = i;
        }
        return Math.max(maxLength, length);
    }

    public static void reset(byte[] arr) {
        for (int i : arr) {
            arr[i] = 0;
        }
    }

    public static void main(String[] args) {
        String s = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!\\\"#$%&'()*+,-./:;<=>?@[\\\\]^_`{|}~ abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!\\\"#$%&'()*+,-./:;<=>?@[\\\\]^_`{|}~ abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!\\\"#$%&'()*+,-./:;<=>?@[\\\\]^_`{|}~ ";
        int max = 0;
        for (int i = 0; i < s.length(); i++) {
            max = s.charAt(i) > max ? s.charAt(i) : max;
        }
        System.out.println(max);
        System.out.println((char)max);
//        System.out.println(lengthOfLongestSubstring());
    }
}
