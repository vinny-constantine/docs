package com.dover.aspectdemo.service;

import com.dover.aspectdemo.annotation.Mark;
import com.dover.aspectdemo.config.UserProps;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author dover
 * @since 2022/7/29
 */
@Service
public class FooService1 {

    @Resource
    private FooService2 fooService2;


    public FooService1() {
        System.out.println("FooService1 created...");
    }

    @Mark
    public void run(UserProps userProps) {
        System.out.println("com.dover.aspectdemo.service.FooService1.run");
        System.out.println(userProps.getUser().get("foo"));
    }

    public static void main(String[] args) {

        String s = "1624332998600,1624332998623,1624332998628,1624332998637,1624332998690,1624332998719,1624332998724,1624332998740,1624332998763,1624332998773,1624332998781,1624332998786,1624332998798,1624332998805,1624332998831,1624332998835,1624332998867,1624332998883,1624332998893,1624333033372,1636474709920,1654188382477,1659604270137";
        Arrays.stream(s.split(",")).forEach(x -> {
            System.out.println(String.format(
                "INSERT INTO drp_service_reservation.distribute_employee_office (employee_id, office_id, office_type, status, tenant_code, created_time, created_by, last_modified_time, last_modified_by, disabled_time, enabled, version, remarks) VALUES (1660818877755, %s, 2, 1, 'IDOPPO', now(), NULL, now(), NULL, NULL, 1, 1, '数据同步');",
                x));

        });


    }
}
