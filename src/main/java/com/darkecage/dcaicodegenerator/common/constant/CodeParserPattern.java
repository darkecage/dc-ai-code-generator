package com.darkecage.dcaicodegenerator.common.constant;

import java.util.regex.Pattern;

/**
 * @Description:
 * @Author: kaiqi.hu
 * @Date: 2026-05-09  15:03
 */
public class CodeParserPattern {
    public static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    public static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    public static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
}
