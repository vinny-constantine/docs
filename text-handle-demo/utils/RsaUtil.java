package com.dover.util;


import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author dover
 * @since 2022/6/28
 **/
@Component
public class RsaUtil {
    private static final int CRYPTO_BITS = 2048;
    private static final String CRYPTO_METHOD = "RSA";
    private static final String CYPHER = "RSA/ECB/PKCS1Padding";
    private static final String RSA_PUBLIC_KEY = "RSA.public-key";
    private static final String RSA_PRIVATE_KEY = "RSA.private-key";

    private static final String RSA_PC_PRIVATE_KEY = "rsa.privateKey";
    private static String DEFAULT_PUBLIC_KEY = "1";
    private static String DEFAULT_PRIVATE_KEY = "2";
    //private static String DEFAULT_PRIVATE_KEY = "3";


    public static void main(String[] args) throws Exception {
//        getKeyPair();
//        System.out.println("pub_key: " + DEFAULT_PUBLIC_KEY);
//        System.out.println("==============================================================================");
//        System.out.println("private_key: " + DEFAULT_PRIVATE_KEY);
//        System.out.println("==============================================================================");
//        System.out.printf("%s=%s%n", "17626042021", RsaUtil.encrypt("17626042021"));
//        System.out.printf("%s=%s%n", "13851874332", RsaUtil.encrypt("13851874332"));
//        System.out.println("==============================================================================");


        String s = RsaUtil.decryptPC("aHj9Kc82V0ebhUCFnUuUeO1SSUAF3DUqGPih/1rZ2jD5jfnFwvOmKkaArkKVgVcxXCs/La4wDLoy4yyETL4EwLLghkry6A5LeyH+TtgZSfzTMEPtquOGI6jiEa8WK9LnvSxRkCxNqj8WJUKEv6vl5gMRf4GJpGlZ04V+CvlR6EnhMn7k2ohx96re4brLziv2OAnT+BP8BiZYpeg6E9OBbwn3rJ3aF9NbBgnoNnxvCfbjLIGZLch3BWO31jNBETD4POobNZoKDTaCzPX/tSN6rYxXd5gXnfAQwKD7sD5O1L+MdgA1XJnmJSG3bxPbhGUHk3Etf2mLcC+efLYbtgy6gg==");
        System.out.println(s);
//        System.out.println(RsaUtil.decrypt(encryptedMessage));
//        System.out.println(RsaUtil.decrypt(
//            "tmZJDzGqgjR6sIJgVT0RMfLu1BkoQP0YiwO4aPlyP96LlSyxyqUCk36bFmtIRJtgeeHXxG0X1pWRQsL/mzkQtJLj+inRhs5lSKhYtUHnd8alkMTbS3VoAIEUYcL9InbpcDgRMw7PuEIpFltBmGqQJs4XyEy2/TIbU6/Pib/C8UeI6HJL6qDtRfhsfADJcuXyr3lcZ3h5B9R//TeteNgwHm1dpPCXUsiBK4txX3G7ARDoKdj2j7MN0flLXX7yV+pe6EA3g8QGdiKDVNc4ksMb1/sIpQ84MOpxRi75eQZ7e1gvGZlWyJrZAXYxMUKcZQmw0Ad0XV28zBJzepb/JIyddQ=="));
    }

    public static void getKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(CRYPTO_METHOD);
        kpg.initialize(CRYPTO_BITS);
        KeyPair kp = kpg.generateKeyPair();
        PublicKey publicKey = kp.getPublic();
        byte[] publicKeyBytes = publicKey.getEncoded();
        DEFAULT_PUBLIC_KEY = Base64.encode(publicKeyBytes);
        PrivateKey privateKey = kp.getPrivate();
        byte[] privateKeyBytes = privateKey.getEncoded();
        DEFAULT_PRIVATE_KEY = Base64.encode(privateKeyBytes);
    }

    public static String encrypt(String clearText) {
        try {
            KeyFactory keyFac = KeyFactory.getInstance(CRYPTO_METHOD);
            KeySpec keySpec = new X509EncodedKeySpec(
                    Base64.decode(DoverProperty.get(RSA_PUBLIC_KEY, DEFAULT_PUBLIC_KEY).trim()));
            Key key = keyFac.generatePublic(keySpec);
            final Cipher cipher = Cipher.getInstance(CYPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(clearText.getBytes(StandardCharsets.UTF_8));
            return Base64.encode(encryptedBytes).replaceAll("([\\r\\n])", "");
        } catch (Exception e) {
            throw new DoverServiceException(ReserveResultCode.ENCRYPTED_ERROR);
        }
    }

    public static String decrypt(String encryptedBase64) {
        try {
            KeyFactory keyFac = KeyFactory.getInstance(CRYPTO_METHOD);
            KeySpec keySpec = new PKCS8EncodedKeySpec(
                    Base64.decode(DoverProperty.get(RSA_PRIVATE_KEY, DEFAULT_PRIVATE_KEY).trim()));
            Key key = keyFac.generatePrivate(keySpec);
            final Cipher cipher = Cipher.getInstance(CYPHER);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedBytes = Base64.decode(encryptedBase64);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DoverServiceException(ReserveResultCode.DECRYPTED_ERROR, e);
        }
    }

    public static String decryptPC(String encryptedBase64) {
        try {
            KeyFactory keyFac = KeyFactory.getInstance(CRYPTO_METHOD);
            KeySpec keySpec = new PKCS8EncodedKeySpec(
                    Base64.decode(DoverProperty.get(RSA_PC_PRIVATE_KEY, DEFAULT_PRIVATE_KEY).trim()));
            Key key = keyFac.generatePrivate(keySpec);
            final Cipher cipher = Cipher.getInstance(CYPHER);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedBytes = Base64.decode(encryptedBase64);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DoverServiceException(ReserveResultCode.DECRYPTED_ERROR, e);
        }
    }
}
