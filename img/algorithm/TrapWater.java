package com.dover.pdf;

/**
 * @author dover
 * @since 2023/2/20
 */
public class TrappingRainWater {

    public static void main(String[] args) {
        int[] arr = {0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1};
        System.out.println(trap(arr));
    }

    public static int trap(int[] height) {
        // 1.找最大高度，并计算原数组高度之和
        // 2.从最大高度开始，降低高度，双指针从头尾开始查找等于当前高度的位置，找到，或指针开始相交，则结束，计算当前高度共多少格子
        // 3.所有格子相加并减去，原数组之和，则为雨水格子数量
        int maxH = 0;
        int sumH = 0;
        for (int j : height) {
            if (j > maxH) maxH = j;
            sumH += j;
        }
        int allUnitCount = 0;
        for (int i = maxH; i >= 1; i--) {
            int head = 0;
            int tail = height.length - 1;
            while (head < tail) {
                if (height[head] >= i && height[tail] >= i) {
                    allUnitCount = allUnitCount + tail - head + 1;
                    break;
                }
                if (height[head] < i) head++;
                if (height[tail] < i) tail--;

            }
            if (head >= tail) allUnitCount++;
        }
        return allUnitCount - sumH;
    }
}
