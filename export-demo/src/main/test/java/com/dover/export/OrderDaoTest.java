package com.dover.export.dao;

import com.dover.export.entity.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderDaoTest {

    private static final Integer[] customerIdList = {103, 112, 114, 119, 121, 124, 125, 128, 129, 131, 141, 144, 145,
            146, 148, 151, 157, 161, 166, 167, 168, 169, 171, 172, 173, 175, 177, 181, 186, 187, 189, 198, 201, 202,
            204, 205, 206, 209, 211, 216, 219, 223, 227, 233, 237, 239, 240, 242, 247, 249, 250, 256, 259, 260, 273,
            276, 278, 282, 286, 293, 298, 299, 303, 307, 311, 314, 319, 320, 321, 323, 324, 328, 333, 334, 335, 339,
            344, 347, 348, 350, 353, 356, 357, 361, 362, 363, 369, 376, 379, 381, 382, 385, 386, 398, 406, 409, 412,
            415, 424, 443, 447, 448, 450, 452, 455, 456, 458, 459, 462, 465, 471, 473, 475, 477, 480, 481, 484, 486,
            487, 489, 495, 496};
    private static final char[] chars = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
            'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    @Autowired
    private OrderDao orderDao;


    @Test
    void deleteByPrimaryKey() {
    }

    @Test
    void insert() {
        Random random = new Random();
        for (int i = 0; i < 1000 * 1000 * 10; i++) {
            LocalDate orderDate = LocalDate.of(random.nextInt(21) + 2000, random.nextInt(12) + 1, random.nextInt(28) + 1);
            orderDao.insert(Order.builder()
                    .orderDate(orderDate)
                    .requiredDate(orderDate)
                    .shippedDate(orderDate)
                    .status("Shipped")
                    .comments(randomStr(random.nextInt(25)))
                    .customerNumber(customerIdList[random.nextInt(customerIdList.length)])
                    .build());
        }

    }

    public static String randomStr(int length) {
        StringBuilder s = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            s.append(chars[random.nextInt(52)]);
        }
        return s.toString();
    }

    @Test
    void insertSelective() {
    }

    @Test
    void selectByPrimaryKey() {

    }

    @Test
    void selectByOrderDate() {
        List<Order> orders = orderDao.selectByOrderDate(LocalDate.of(2004, 10, 29));
        System.out.println(orders);
    }

    @Test
    void updateByPrimaryKeySelective() {
    }

    @Test
    void updateByPrimaryKey() {
    }
}