package com.dover;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author dover
 * @since 2023/4/28
 */
public class DeadLockDemo {


    public void initTable() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
        // 清空表数据
        String truncateDept = "drop TABLE if exists t_dept";
        String truncateUser = "drop TABLE if exists t_user ";
        conn.createStatement().execute(truncateDept);
        conn.createStatement().execute(truncateUser);

        String deptSql = "CREATE TABLE t_dept (dept_id INT PRIMARY KEY, dept_name VARCHAR(20))";
        String userSql = "CREATE TABLE t_user  (user_id INT PRIMARY KEY, user_name VARCHAR(20), ref_dept_id INT)";

        conn.createStatement().execute(deptSql);
        conn.createStatement().execute(userSql);

        // 初始化 t_dept 表数据
        String insertDept = "INSERT INTO t_dept VALUES (1, '研发部'), (2, '市场部')";
        conn.createStatement().execute(insertDept);

        // 初始化 t_user  表数据
        String insertUser = "INSERT INTO t_user  VALUES (1, '张三', 1), (2, '李四', 2)";
        conn.createStatement().execute(insertUser);

        // 将事务隔离级别调整为 READ COMMITTED
        String sql = "SET TRANSACTION ISOLATION LEVEL READ COMMITTED;";
        conn.createStatement().execute(sql);
        conn.close();
    }

    public void thread1() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
            conn.setAutoCommit(false);

            // 查询 A 表
            String sql1 = "SELECT * FROM t_dept WHERE dept_id = ?";
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setInt(1, 1);
            ResultSet rs1 = ps1.executeQuery();

            // 查询 B 表
            String sql2 = "SELECT * FROM t_user  WHERE ref_dept_id = ?";
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.setInt(1, 1);
            ResultSet rs2 = ps2.executeQuery();

            // 更新 A 表
            String sql3 = "UPDATE t_dept SET dept_name = ? WHERE dept_id = ?";
            PreparedStatement ps3 = conn.prepareStatement(sql3);
            ps3.setString(1, "研发部");
            ps3.setInt(2, 1);
            ps3.executeUpdate();

            // 更新 B 表
            String sql4 = "UPDATE t_user  SET user_name = ? WHERE ref_dept_id = ?";
            PreparedStatement ps4 = conn.prepareStatement(sql4);
            ps4.setString(1, "张三");
            ps4.setInt(2, 1);
            ps4.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("发生死锁,线程 ID:" + Thread.currentThread().getId());
        } finally {

            try {
                conn.close();
            } catch (SQLException e) {

            }
        }
    }

    public static void main(String[] args) throws Exception {
        DeadLockDemo test = new DeadLockDemo();
        test.initTable();

        for (int i = 0; i < 101; i++) {  // 重复创建 10 个线程
            Thread thread1 = new Thread(() -> test.thread1());
            thread1.start();
        }
        // 等待所有子线程执行完成
        for (int i = 0; i < 101; i++) {
            Thread.sleep(500);
        }
    }
}
