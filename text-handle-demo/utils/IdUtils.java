package com.dover.util;

import cn.hutool.core.util.IdUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 53 bits unique id:
 * <p>
 * |--------|--------|--------|--------|--------|--------|--------|--------|
 * |00000000|00011111|11111111|11111111|11111111|11111111|11111111|11111111|
 * |--------|---xxxxx|xxxxxxxx|xxxxxxxx|xxxxxxxx|xxx-----|--------|--------|
 * |--------|--------|--------|--------|--------|---xxxxx|xxxxxxxx|xxx-----|
 * |--------|--------|--------|--------|--------|--------|--------|---xxxxx|
 * <p>
 * Maximum ID = 11111_11111111_11111111_11111111_11111111_11111111_11111111
 * <p>
 * Maximum TS = 11111_11111111_11111111_11111111_111
 * <p>
 * Maximum NT = ----- -------- -------- -------- ---11111_11111111_111 = 65535
 * <p>
 * Maximum SH = ----- -------- -------- -------- -------- -------- ---11111 = 31
 * <p>
 * It can generate 64k unique id per IP and up to 2106-02-07T06:28:15Z.
 */
public class IdUtils {

    private static final Logger logger = LoggerFactory.getLogger(IdUtil.class);

    private static final Pattern PATTERN_LONG_ID = Pattern.compile("^([0-9]{15})([0-9a-f]{32})([0-9a-f]{3})$");

//    private static final Pattern PATTERN_HOSTNAME = Pattern.compile("^.*\\D+([0-9]+)$");

    private static final long OFFSET = LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.of("Z")).toEpochSecond();

    private static final long MAX_NEXT = 0b11111_11111111_111L;
    /**
     * 服务启动向redis上报自身serverId，若冲突，则自增，直至不冲突
     */
    private static final String SERVER_ID = "reserve-union:server-id:";
    // 改为懒初始化
    private static long SHARD_ID = 32;

    private static long offset = 0;

    private static long lastEpoch = 0;

    public static String nextIdStr() {
        return String.valueOf(nextId(System.currentTimeMillis() / 1000));
    }

    public static Long nextId() {
        return nextId(System.currentTimeMillis() / 1000);
    }

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static synchronized long nextId(long epochSecond) {
        if (epochSecond < lastEpoch) {
            // warning: clock was turn back:
            logger.warn("clock has been back: " + epochSecond + " from previous:" + lastEpoch);
            epochSecond = lastEpoch;
        }
        if (lastEpoch != epochSecond) {
            lastEpoch = epochSecond;
            reset();
        }
        offset++;
        long next = offset & MAX_NEXT;
        if (next == 0) {
            logger.warn("maximum id reached in 1 second in epoch: " + epochSecond);
            return nextId(epochSecond + 1);
        }
        return generateId(epochSecond, next, getServerIdAsLong());
    }

    private static void reset() {
        offset = 0;
    }

    private static long generateId(long epochSecond, long next, long shardId) {
        return ((epochSecond - OFFSET) << 21) | (next << 5) | shardId;
    }

    private static long getServerIdAsLong() {
        // shardId 只可能是 [0 , 31] 之间的值
        if (SHARD_ID != 32) return SHARD_ID;
        try {
            // 生成 serverId 后，尝试上报 serverId 至 redis，若上报成功，则使用该 serverId，若失败，则自增重新上报
            String hostname = InetAddress.getLocalHost().getHostName();
            int serverId = Math.abs(hostname.hashCode()) % 31;
            for (int i = 0; i < 32; i++) {
                boolean res = DoverRedisCommon.setNx(SERVER_ID + serverId, 1);
                if (res) {
                    DoverLog.info("hostname={}，serverId={}，上报成功", hostname, serverId);
                    // 上报成功后，将serverId设置为一天失效（一般union+union2c两个项目会一起发布，共4台主机，一天之内全部重新部署8次也不会冲突）
                    DoverRedisCommon.expire(SERVER_ID + serverId, 60 * 60 * 24);
                    SHARD_ID = serverId;
                    return SHARD_ID;
                }
                DoverLog.info("hostname={}，serverId={}，上报失败", hostname, serverId);
                serverId = serverId == 31 ? 0 : serverId + 1;
            }
        } catch (UnknownHostException e) {
            logger.warn("无法获取主机名，将 serverId 设置为随机值 random(0, 31)");
        }
        Random random = new Random();
        int serverId = random.nextInt(32);
        DoverLog.info("根据hostname生成serverId上报已达上限，随机生成serverId={}", serverId);
        SHARD_ID = serverId;
        return serverId;
    }

    public static long stringIdToLongId(String stringId) {
        // a stringId id is composed as timestamp (15) + uuid (32) + serverId (000~fff).
        Matcher matcher = PATTERN_LONG_ID.matcher(stringId);
        if (matcher.matches()) {
            long epoch = Long.parseLong(matcher.group(1)) / 1000;
            String uuid = matcher.group(2);
            byte[] sha1 = DigestUtils.sha1(uuid.getBytes(StandardCharsets.UTF_8));
            long next = ((sha1[0] << 24) | (sha1[1] << 16) | (sha1[2] << 8) | sha1[3]) & MAX_NEXT;
            long serverId = Long.parseLong(matcher.group(3), 16);
            return generateId(epoch, next, serverId);
        }
        throw new DoverServiceException("invalid id");
    }
}
