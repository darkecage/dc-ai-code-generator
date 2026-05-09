package com.darkecage.dcaicodegenerator.ai.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description:
 * @Author: kaiqi.hu
 * @Date: 2026-05-09  15:10
 */
public class ExtractCodeUtils {
    /**
     * 根据正则模式提取代码
     *
     * @param content 原始内容
     * @param pattern 正则模式
     * @return 提取的代码
     */
    public static String extractCodeByPattern(String content, Pattern pattern) {
        if (content == null) return null;
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
