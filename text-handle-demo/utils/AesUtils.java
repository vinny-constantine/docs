package com.dover.util;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.UUID;
import java.util.function.Function;

/**
 * AES对称加密，对明文进行加密、解密处理，推荐使用AES_CBC_256, 数据库使用的是 AES_ECB_128
 *
 * @author dover
 */
public class AesUtils {
    private static final int IV_SIZE_16 = 16;
    private static final int AES_KEY_LENGTH_128 = 128;
    private static final int AES_KEY_LENGTH_256 = 256;
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_ECB_PKCS5PADDING_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String SECURE_RANDOM_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String AES_CBC_PKCS5PADDING_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String AES_KEY = "AES.key";
    private static final String DEFAULT_KEY = "RU1jNUdQU0RVTnVqekhOU3pWY3ZTTFpYZHViWjNrSEs=";


    @SneakyThrows
    public static void main(String[] args) {
//        System.out.println(createBase64EncodedAES256Key());
        System.out.println(AesUtils.encrypt("15804926464"));
        System.out.println(AesUtils.encrypt("15642266788"));
//        System.out.println(AesUtils.decrypt("ho0fNpD2s5VKuZUPzn2gSw=="));
    }

    /**
     * 使用Aes128 加密  PaddingMode = PKCS5, CipherMode = ECB
     *
     * @param text 明文
     * @return Base64编码后的加密密文
     */
    public static String encrypt(String text) {
        try {
            String key = DoverProperty.get(AES_KEY, DEFAULT_KEY);
            Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5PADDING_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, generateMySQLAESKey(key, Base64::decodeBase64));
            byte[] encryptData = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encryptData);
        } catch (Exception e) {
            throw new DoverServiceException(ReserveResultCode.ENCRYPTED_ERROR);
        }
    }

    /**
     * 使用Aes128 解密  PaddingMode = PKCS5, CipherMode = ECB
     *
     * @param encryptedText Base64编码后的加密密文
     * @return 明文
     */
    public static String decrypt(String encryptedText) {
        try {
            String key = DoverProperty.get(AES_KEY, DEFAULT_KEY);
            Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5PADDING_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, generateMySQLAESKey(key, Base64::decodeBase64));
            byte[] encryptData = cipher.doFinal(Base64.decodeBase64(encryptedText));
            return new String(encryptData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DoverServiceException(ReserveResultCode.DECRYPTED_ERROR);
        }
    }

    /**
     * mySQL特殊的Key生成方式，必须是128位即16个字节
     * 参考 https://dev.mysql.com/doc/refman/8.0/en/encryption-functions.html#function_aes-decrypt,
     * https://stackoverflow.com/questions/19518447/aes-encryption-method-equivalent-to-mysql-aes-encrypt-function
     *
     * @param key
     * @param encoding
     * @return
     */
    private static SecretKeySpec generateMySQLAESKey(final String key, Function<String, byte[]> encoding) {
        final byte[] finalKey = new byte[32];
        int i = 0;
        for (byte b : encoding.apply(key))
            finalKey[i++ % 32] ^= b;
        return new SecretKeySpec(finalKey, AES_ALGORITHM);
    }

    /**
     * 使用Aes256 加密  PaddingMode = PKCS5, CipherMode = CBC
     *
     * @param text 明文
     * @param key  256位 byte 使用Base64编码后的字符串
     * @param iv   Base64编码后的偏移量
     * @return Base64编码后的加密密文
     */
    public static String AesCbc256Encrypt(String text, String key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5PADDING_ALGORITHM);
        byte[] keyBytes = Base64.decodeBase64(key);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, AES_ALGORITHM),
            new IvParameterSpec(Base64.decodeBase64(iv)));
        byte[] encryptData = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64String(encryptData);
    }

    /**
     * 使用Aes256 解密  PaddingMode = PKCS5, CipherMode = CBC
     *
     * @param encryptedText Base64编码后的加密密文
     * @param key           256位 byte 使用Base64编码后的字符串
     * @param iv            Base64编码后的偏移量
     * @return 明文
     */
    public static String AesCbc256Decrypt(String encryptedText, String key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5PADDING_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Base64.decodeBase64(key), AES_ALGORITHM),
            new IvParameterSpec(Base64.decodeBase64(iv)));
        byte[] encryptData = cipher.doFinal(Base64.decodeBase64(encryptedText));
        return new String(encryptData, StandardCharsets.UTF_8);
    }

    /**
     * 生成IV Byte[16]
     *
     * @return
     */
    public static IvParameterSpec createIV() throws Exception {
        final byte[] iv = new byte[IV_SIZE_16];
        SecureRandom random = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM);
        random.setSeed(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        random.nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    /**
     * 生成IV Byte[16]
     *
     * @return Base64编码的字符串
     */
    public static String createBase64EncodedIV() throws Exception {
        final byte[] iv = new byte[IV_SIZE_16];
        SecureRandom random = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM);
        random.setSeed(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        random.nextBytes(iv);
        return Base64.encodeBase64String(iv);
    }

    /**
     * 生成Aes 128 KEY
     *
     * @return Base64编码的字符串
     * @throws Exception
     */
    public static String createBase64EncodedAES128Key() throws Exception {
        byte[] keys = createAESKey(AES_KEY_LENGTH_128).getEncoded();
        return Base64.encodeBase64String(keys);
    }

    /**
     * 生成Aes 256 KEY
     *
     * @return Base64编码的字符串
     * @throws Exception
     */
    public static String createBase64EncodedAES256Key() throws Exception {
        byte[] keys = createAESKey(AES_KEY_LENGTH_256).getEncoded();
        return Base64.encodeBase64String(keys);
    }

    /**
     * 生成Aes 128 KEY
     *
     * @return
     * @throws Exception
     */
    public static SecretKey createAES128Key() throws Exception {
        return createAESKey(AES_KEY_LENGTH_128);
    }

    /**
     * 生成Aes 256 KEY
     *
     * @return
     * @throws Exception
     */
    public static SecretKey createAES256Key() throws Exception {
        return createAESKey(AES_KEY_LENGTH_256);
    }

    /**
     * 生成Aes KEY
     *
     * @param keySize KEY的大小: 128 or 256
     * @return
     * @throws Exception
     */
    private static SecretKey createAESKey(int keySize) throws Exception {
        String uuid = UUID.randomUUID().toString();
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECURE_RANDOM_ALGORITHM);
        KeySpec spec = new PBEKeySpec(uuid.toCharArray(), uuid.getBytes(StandardCharsets.UTF_8), 65536, keySize);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), AES_ALGORITHM);
    }
}
