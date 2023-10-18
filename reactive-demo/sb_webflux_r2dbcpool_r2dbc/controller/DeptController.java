package com.example.reactive.controller;

import com.example.reactive.domain.Department;
import com.example.reactive.domain.Employee;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author dover
 * @since 2023/8/26
 */
public class DeptController {


    @ExceptionHandler
    public String handleError(RuntimeException ex) { // ... }
        return null;
    }

    @GetMapping("/contact/{deptId}/employee/{empId}")
    public Employee findEmployee(@PathVariable Long deptId, @PathVariable Long empId) {// Find the employee.}

        return null;
    }


//    @GetMapping("/contact/employee")
//    public Employee findEmployee(@RequstParam("deptId") Long deptId, @RequstParam("empId") Long empId) {
//        // Find the employee.
//        return null;
//    }


    @GetMapping("/fibonacci")
    public List<Long> fibonacci(@RequestHeader("Accept-Encoding") String encoding) {// Determine Series

        return null;
    }

    @PostMapping("/department")
    public void createDept(@RequestBody Mono<Department> dept) {
        // Add new department

    }

    @PostMapping("/department")
    public void createdept(@ModelAttribute Department dept) {
        // Add new department
    }
}