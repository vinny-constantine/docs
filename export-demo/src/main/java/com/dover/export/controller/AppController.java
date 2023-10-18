package com.dover.export.controller;

import com.dover.export.dao.OrderChunk;
import com.dover.export.entity.Order;
import com.dover.export.utils.ExcelStreamingUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wangwei
 * @since 2020/11/19
 */
@RestController
public class AppController {

    private ExecutorService executorService = Executors.newFixedThreadPool(10);


    @GetMapping("/happens")
    public void happensBefore() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> executorService.submit(
                () -> System.out.println(LocalDateTime.now() + " 执行了 " + Thread.currentThread().getName()))).start();
        }
        for (int i = 0; i < 10; i++) {
            executorService.submit(
                () -> System.out.println(LocalDateTime.now() + " 执行了 " + Thread.currentThread().getName()));
        }
        System.out.println("添加线程结束");
        Thread.sleep(1000);
        System.out.println("sleep结束");
        executorService.submit(
            () -> System.out.println(LocalDateTime.now() + " 执行了 " + Thread.currentThread().getName()));
    }

    @PostMapping("/export")
    public ResponseEntity<StreamingResponseBody> export() {
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                String.format("attachment; filename=order-%s.csv", LocalDateTime.now()))
            .body(outputStream -> ExcelStreamingUtil.export(outputStream,
                new OrderChunk(2, Order.builder().status("Cancelled").build())));
    }
}
