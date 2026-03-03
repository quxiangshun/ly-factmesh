package com.ly.factmesh.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 摘要工具类（MD5、SHA256）
 *
 * @author LY-FactMesh
 */
public final class DigestUtils {

    private DigestUtils() {
    }

    public static String md5Hex(String input) {
        return digestHex("MD5", input);
    }

    public static String sha256Hex(String input) {
        return digestHex("SHA-256", input);
    }

    private static String digestHex(String algorithm, String input) {
        if (input == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("不支持的算法: " + algorithm, e);
        }
    }
}
