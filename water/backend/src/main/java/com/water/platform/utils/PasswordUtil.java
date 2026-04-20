package com.water.platform.utils;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * 密码加密工具类
 * 使用 MD5 + 随机盐值加密
 * 存储格式：盐值$加密结果
 */
public class PasswordUtil {

    private static final String SEPARATOR = "$";

    /**
     * 加密密码
     * @param rawPassword 明文密码
     * @return 加密后的密码（格式：盐值$哈希值）
     */
    public static String encrypt(String rawPassword) {
        String salt = DigestUtil.md5Hex(String.valueOf(System.currentTimeMillis())).substring(0, 16);
        String hash = DigestUtil.md5Hex(salt + rawPassword);
        return salt + SEPARATOR + hash;
    }

    /**
     * 验证密码
     * @param rawPassword 明文密码
     * @param encryptedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean verify(String rawPassword, String encryptedPassword) {
        // 兼容明文密码（旧数据自动迁移）
        if (!encryptedPassword.contains(SEPARATOR)) {
            return rawPassword.equals(encryptedPassword);
        }
        String[] parts = encryptedPassword.split("\\$");
        if (parts.length != 2) {
            return false;
        }
        String salt = parts[0];
        String hash = parts[1];
        return DigestUtil.md5Hex(salt + rawPassword).equals(hash);
    }

    /**
     * 判断密码是否已加密（是否需要迁移）
     * @param encryptedPassword 存储的密码
     * @return true=已加密，false=明文
     */
    public static boolean isEncrypted(String encryptedPassword) {
        return encryptedPassword != null && encryptedPassword.contains(SEPARATOR);
    }
}
