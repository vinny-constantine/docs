
import com.alibaba.ttl.TransmittableThreadLocal;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * @see TraceIdFilter
 */
public final class TraceIdUtil {

    public static final String TRACE_ID = "trace-id";

    public static final TransmittableThreadLocal<String> TRACE_ID_THREAD_LOCAL = new TransmittableThreadLocal<>();

    /**
     * 设置并生成traceId
     */
    public static void setTraceId() {
        String traceId = UUID.randomUUID().toString().toLowerCase().replace("-", "");
        TRACE_ID_THREAD_LOCAL.set(traceId);
        MDC.put(TRACE_ID, traceId);
    }

    /**
     * 设置traceId
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    /**
     * 从父线程中继承traceId
     * 仅在使用了TtlExecutors.getTtlExecutorService包装后的子线程中调用，其他时候不应该调用
     */
    public static void inheritTraceId() {
        setTraceId(TRACE_ID_THREAD_LOCAL.get());
    }

    /**
     * 获取当前线程的traceId
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    /**
     * 移除当前线程的traceId
     */
    public static void removeTraceId() {
        MDC.remove(TRACE_ID);
    }
}