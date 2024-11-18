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
 * @author dover
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
            .header(HttpHeaders.CONTENT_DISPOSITION,
                String.format("attachment; filename=order-%s.csv", LocalDateTime.now()))
            .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .body(outputStream -> ExcelStreamingUtil.export(outputStream,
                new OrderChunk(2, Order.builder().status("Cancelled").build())));
    }

    
    /**
     * 转发excel流
     */
    @PostMapping("/uploadExcelFile")
    public JSONObject uploadExcelFile(String groupId, MultipartFile file, @RequestHeader Map<String, String> headerMap) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headerMap.entrySet().forEach(x -> headers.add(x.getKey(), String.valueOf(x.getValue())));
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("groupId", groupId);
            body.add("file", new UploadedByteArrayResource(file.getOriginalFilename(), file.getBytes()));
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);
            return restTemplateService.postForJson(urlUtinls.getDispatchAdminUrl() + "/uploadFile", httpEntity);
        } catch (IOException e) {
            log.error("转发excel流失败")
        }
    }

        
    /**
     * @author dover
     */
    public static class UploadedByteArrayResource extends ByteArrayResource {

        private String filename;

        public UploadedByteArrayResource(byte[] byteArray) {
            super(byteArray);
        }

        public UploadedByteArrayResource(String filename, byte[] byteArray) {
            super(byteArray);
            this.filename = filename;
        }

        public UploadedByteArrayResource(byte[] byteArray, String description) {
            super(byteArray, description);
        }

        @Override
        public String getFilename() {
            return this.filename;
        }

    }
}
