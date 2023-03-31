import java.util.Scanner;

import java.util.*;

// 注意类名必须为 Main, 不要有任何 package xxx 信息
public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        // w[i] 代表当前物品花费金额，v[i] 代表当前物品满意度
        // d[i][j] 代表前 i 件物品，共花费 j 元的最大满意度
        // d[i][j] = max{ d[i-1][j], d[i-1][j-w[i]] + v[i] }
        // w[i][k] 代表购买当前主件携带附件场景的花费，v[i][k] 代表购买当前主件携带附件场景满意度，
        // 其中 k in (0, 1, 2, 3) 分别代表 0: 不购买附件，1：购买附件1，2：购买附件2，3：购买附件1附件2
        // d[i][j] = max{ d[i-1][j], d[i-1][j-w[i][k]] + v[i][k] }
        String[] costAndQty = in.nextLine().split(" ");
        int cost = Integer.parseInt(costAndQty[0]);
        int qty = Integer.parseInt(costAndQty[1]);
        List<int[]> lineList = new ArrayList<int[]>(qty);
        List<Integer> mainIdxList = new ArrayList<Integer>();
        for(int i = 0; i < qty; i++){
            String[] tmp = in.nextLine().trim().split(" ");
            lineList.add(new int[]{Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]),Integer.parseInt(tmp[2])});
            if(tmp[2].equals("0")) mainIdxList.add(i);
        }
        int[][] w = new int[mainIdxList.size()][4];
        int[][] v = new int[mainIdxList.size()][4];
        for(int i = 0; i < mainIdxList.size(); i++) {
            int idx = mainIdxList.get(i);
            int[] line = lineList.get(idx);
            w[i][0] = line[0];
            v[i][0] = line[0] * line[1];
        }
        for(int[] line : lineList) {
            if(line[2] != 0){
                int i = mainIdxList.indexOf(line[2] - 1);
                if(w[i][1] == 0) { // 主要件 + 第一个附件
                    w[i][1] = w[i][0] + line[0];
                    v[i][1] = v[i][0] + line[0] * line[1];
                } else { 
                    // 主要件 + 第二个附件
                    w[i][2] = w[i][0] + line[0];
                    v[i][2] = v[i][0] + line[0] * line[1];
                    // 主要件 + 第一个附件 + 第二个附件
                    w[i][3] = w[i][1] + line[0];
                    v[i][3] = v[i][1] + line[0] * line[1];
                } 
            }
        }
        // 开始计算dp
        int[][] dp = new int[mainIdxList.size()][cost/10 + 1];
        for(int i = 0; i < mainIdxList.size(); i++) {
            int[] tmpW = w[i];
            int[] tmpV = v[i];
            for(int j = 0; j <= cost/10; j++) {
                // 当前物品不放入，且花费为 j 元时的最大满意度
                dp[i][j] = dp[i-1<0?0:i-1][j];
                // 比较附件1和附件2的花费
                if(tmpW[0] <= j) {// 可以买主要件了
                    dp[i][j] = Math.max(dp[i][j], dp[i][j] + v[i][0]);
                }
                if(tmpW[1] <= j) {// 可以买主要件 + 附件1了
                    dp[i][j] = Math.max(dp[i][j], dp[i][j] + v[i][0]);
                }
                
            }
        }
        System.out.println(mainIdxList);
        for(int[] line : lineList) {
            System.out.println(Arrays.toString(line));
        }
        for(int i = 0; i < w.length; i++) {
            System.out.println(Arrays.toString(w[i]));
        }
        for(int i = 0; i < v.length; i++) {
            System.out.println(Arrays.toString(v[i]));
        }
    }
}