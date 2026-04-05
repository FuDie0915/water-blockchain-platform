package com.water.platform.utils;

import cn.hutool.core.util.RandomUtil;

import java.util.Random;

/**
 * @author ：devon
 * @date ：2024/10/14 8:47
 * @description：随机字符串
 * @version: 1.0
 */
public class RanStringUtil {
    public static String generateRandomString(int length) {
        // 如果你需要生成包含特定字符的随机字符串，可以传入一个字符串作为参数
        String randomStringWithChars = RandomUtil.randomString("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", 100);
        return randomStringWithChars;
    }

}
