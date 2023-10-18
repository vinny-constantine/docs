package com.kardo;

import com.alibaba.fastjson.JSON;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

/**
 * @author dover
 * @since 2023/3/29
 */
@Service
@ConditionalOnClass(JSON.class)
public class CService {

    public CService() {
        System.out.println("CService created...");
        int[] arr = new int[]{1, 2, 3};
        System.out.println(JSON.toJSONString(arr));
    }
}
