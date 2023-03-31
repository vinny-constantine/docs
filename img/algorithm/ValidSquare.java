package com.dover.pdf;

/**
 * @author dover
 * @since 2022/7/29
 */
public class ValidSquare {

    public static void main(String[] args) {
        int[] p1 = {1, 0};
        int[] p2 = {0, 1};
        int[] p3 = {0, -1};
        int[] p4 = {-1, 0};
        System.out.println(new ValidSquare().validSquare(p1, p2, p3, p4));
    }

    public boolean validSquare(int[] p1, int[] p2, int[] p3, int[] p4) {
        double d1 = calcDistance(p1, p2);
        double d2 = calcDistance(p2, p3);
        double d3 = calcDistance(p3, p4);
        double d4 = calcDistance(p4, p1);
        double d5 = calcDistance(p1, p3);
        // 判断顶点重合
        if (d1 == 0 || d2 == 0 || d3 == 0 || d4 == 0 || d5 == 0) return false;
        // 顶点为逆时针或顺时针顺序排列
        if (d1 == d2 && d2 == d3 && d3 == d4) return true;
        if (d1 == d3 && d2 == d4) {
            if (d1 > d2) {// 先计算了对角线
                return d1 == 2 * d2 && d1 == 2 * calcDistance(p2, p4);
            } else {// 先计算了直角边
                return d2 == 2 * d1 && d2 == 2 * calcDistance(p1, p3);
            }
        }
        return false;
    }

    public double calcDistance(int[] p1, int[] p2) {
        return Math.pow(p1[0] - p2[0], 2) + Math.pow(p1[1] - p2[1], 2);
    }


}
