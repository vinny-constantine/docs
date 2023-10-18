package com.dover.util;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author dover
 * @since 2022/11/14
 */
public final class TxUtil {


    /**
     * 注册spring事务提交后逻辑
     */
    public static void afterCommit(Runnable runnable) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }
}
