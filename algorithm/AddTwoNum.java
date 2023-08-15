package com.dover.pdf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dover
 * @since 2022/7/26
 */
public class AddTwoNum {



    public static class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    public static ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode bigListNode = l1;
        ListNode smallListNode = l2;
        while (true) {
            if (l1.next == null) {
                ListNode tmp = smallListNode;
                smallListNode = bigListNode;
                bigListNode = tmp;
                break;
            } else if (l2.next == null) {
                break;
            }
            l1 = l1.next;
            l2 = l2.next;
        }
        ListNode head = new ListNode();
        ListNode point = head;
        int extra = 0;
        while (true) {
            if (bigListNode != null) {
                point.val = bigListNode.val + (smallListNode == null ? 0 : smallListNode.val) + extra;
                extra = point.val / 10;
                point.val = point.val % 10;
            } else {
                if (extra != 0) {
                    point.val = extra;
                }
                break;
            }
            if (!(bigListNode.next == null && extra == 0)) {
                point.next = new ListNode();
            }
            point = point.next;
            bigListNode = bigListNode.next;
            smallListNode = smallListNode == null ? null : smallListNode.next;
        }
        return head;
    }

    public static void main(String[] args) {
        ListNode l1 = new ListNode();
        ListNode l2 = new ListNode();
        String s = "666";
        ListNode h1 = l1;
        ListNode h2 = l2;
        for (int i = 0; i < s.length(); i++) {
            h1.val = s.charAt(i) - 48;
            h2.val = s.charAt(s.length() - i - 1) - 48;
            if (i != s.length() - 1) {
                h1.next = new ListNode();
                h2.next = new ListNode();
                h1 = h1.next;
                h2 = h2.next;
            }
        }
//        l2 = l2.next;
        print(l1);
        print(l2);
        ListNode res = addTwoNumbers(l1, l2);
        print(res);
    }

    public static void print(ListNode res) {
        while (true) {
            System.out.print(res.val);
            res = res.next;
            if (res == null) {
                break;
            }
        }
        System.out.println();
    }
}
