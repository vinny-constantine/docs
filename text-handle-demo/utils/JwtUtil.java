package com.dover.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author dover
 * @since 2022/5/24
 */
public final class JwtUtil {
    /**
     * jwt 签名秘钥
     */
    private static final String SECRET_NAME = "constant.jwt.secret";
    /**
     * token有效时间（秒）
     */
    private static final String EXPIRE = "constant.jwt.expire-day";
    /**
     * 默认secret
     */
    public static final String DEFAULT_SECRET = "111111";

    public static void main(String[] args) {
//        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
//        String secretString = Encoders.BASE64.encode(key.getEncoded());
//        System.out.println(secretString);
        String subject = "13851874332";
        DoverProperty.getPropertyMap().put(EXPIRE, 30);
        System.out.println(generate(subject));
//        Claims claims = verify(
//            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0IiwiaWF0IjoxNjUzNDYzNjk0LCJleHAiOjE2NTM1NTAwOTR9.wQhGAljKbu8miv2D4oMIp7ivAnLsofccO6Ve97T7ipM");
//        System.out.println(claims.getSubject().equals(subject));
    }

    /**
     * 生成jwt
     *
     * @param salerPhoneNo 导购的手机号
     * @return jwt
     */
    public static String generate(String salerPhoneNo) {
        Integer validDays = DoverProperty.getInteger(EXPIRE, ReserveConst.ONE);
        // 过期日期（次日凌晨过期）
        Date expiredDate = new Date(LocalDate.now()
            .plusDays(validDays)
            .atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli());
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(DoverProperty.get(SECRET_NAME, DEFAULT_SECRET)));
        return Jwts.builder()
            .setSubject(salerPhoneNo)
            .setIssuedAt(new Date())
            .setExpiration(expiredDate)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * 验证jwt有效性
     */
    public static Claims verify(String jwt) {
        if (StringUtils.isBlank(jwt)) {
            throw new AuthException(SalerConst.INVALID_SALER_TOKEN);
        }
        try {
            String secret = DoverProperty.get(SECRET_NAME, DEFAULT_SECRET);
            SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
            Jws<Claims> jws = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(jwt);
            return jws.getBody();
        } catch (JwtException ex) {
            throw new AuthException(SalerConst.INVALID_SALER_TOKEN, ex);
        }
    }

}
