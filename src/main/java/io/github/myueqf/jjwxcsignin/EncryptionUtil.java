package io.github.myueqf.jjwxcsignin;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


public final class EncryptionUtil {
    private EncryptionUtil() {}

    /**
     * 使用DES算法加密纯文本。
     *
     * @param keyHex     16进制表示的密钥。
     * @param ivHex      16进制表示的初始化向量 (IV)。
     * @param plaintext  需要加密的纯文本字符串。
     * @return Base64编码的加密后字符串。
     * @throws Exception 如果加密过程中发生错误。
     */
    public static String desEncrypt(String keyHex, String ivHex, String plaintext) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(hexStringToByteArray(keyHex), "DES");
        IvParameterSpec iv = new IvParameterSpec(hexStringToByteArray(ivHex));

        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * 将16进制字符串转换为字节数组。
     *
     * @param hexString 16进制格式的字符串。
     * @return 转换后的字节数组。
     */
    public static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}
