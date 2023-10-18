package com.dover.aspectdemo.common;

import lombok.SneakyThrows;

/**
 * @author dover
 * @since 2021/11/15
 */
public class ThreadDemo {


    public static int count = 0;


    @SneakyThrows
    public static void main(String[] args) {
        // 1.最后拿到锁再 wait 的线程，最先被唤醒，栈的形式，后进先出
        // 2.锁必须再同步块里 notify
        Object lock = new Object();
        Runnable task = () -> {
            synchronized (lock) {
                try {
                    System.out.println(Thread.currentThread().getName() + " wait...");
                    lock.wait();
                    System.out.println(Thread.currentThread().getName() + " say: " + ++count);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(task, "runner1").start();
        new Thread(task, "runner2").start();
        new Thread(task, "runner3").start();
        new Thread(task, "runner4").start();
        new Thread(task, "runner5").start();
        new Thread(task, "runner6").start();
        Thread.sleep(4000);
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
