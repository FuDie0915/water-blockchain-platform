package com.water.platform.utils;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.lang.UUID;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图形验证码工具类
 * 基于 hutool-captcha 生成验证码图片
 * 内存缓存验证码，支持过期清理
 */
public class ImageCaptchaUtil {

    /**
     * 验证码缓存：captchaKey -> { code, expireTime }
     */
    private static final Map<String, CaptchaEntry> CAPTCHA_CACHE = new ConcurrentHashMap<>();

    /**
     * 验证码有效期（毫秒），默认 2 分钟
     */
    private static final long EXPIRE_MS = 2 * 60 * 1000L;

    /**
     * 生成图形验证码
     * @return CaptchaResult 包含 captchaKey（用于验证）和 imageBase64（图片）
     */
    public static CaptchaResult generate() {
        // 清理过期验证码
        cleanExpired();

        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 6);
        String code = captcha.getCode();
        String captchaKey = UUID.fastUUID().toString(true);
        String imageBase64 = "data:image/png;base64," + captcha.getImageBase64();

        CAPTCHA_CACHE.put(captchaKey, new CaptchaEntry(code, System.currentTimeMillis() + EXPIRE_MS));

        return new CaptchaResult(captchaKey, imageBase64);
    }

    /**
     * 验证验证码
     * @param captchaKey 验证码key
     * @param captchaCode 用户输入的验证码
     * @return 是否正确
     */
    public static boolean verify(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaCode == null) {
            return false;
        }
        CaptchaEntry entry = CAPTCHA_CACHE.remove(captchaKey);
        if (entry == null) {
            return false;
        }
        if (System.currentTimeMillis() > entry.expireTime) {
            return false;
        }
        return entry.code.equalsIgnoreCase(captchaCode);
    }

    /**
     * 清理过期验证码
     */
    private static void cleanExpired() {
        long now = System.currentTimeMillis();
        CAPTCHA_CACHE.entrySet().removeIf(e -> now > e.getValue().expireTime);
    }

    private static class CaptchaEntry {
        String code;
        long expireTime;

        CaptchaEntry(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }

    public static class CaptchaResult {
        private final String captchaKey;
        private final String imageBase64;

        public CaptchaResult(String captchaKey, String imageBase64) {
            this.captchaKey = captchaKey;
            this.imageBase64 = imageBase64;
        }

        public String getCaptchaKey() { return captchaKey; }
        public String getImageBase64() { return imageBase64; }
    }
}
