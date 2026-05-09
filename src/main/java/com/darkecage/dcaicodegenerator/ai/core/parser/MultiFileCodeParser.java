package com.darkecage.dcaicodegenerator.ai.core.parser;

import com.darkecage.dcaicodegenerator.ai.model.MultiFileCodeResult;

import static com.darkecage.dcaicodegenerator.ai.core.utils.ExtractCodeUtils.extractCodeByPattern;
import static com.darkecage.dcaicodegenerator.common.constant.CodeParserPattern.*;

/**
 * 多文件代码解析器
 *
 * @author kaiqi.hu
 */
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult>{
    @Override
    public MultiFileCodeResult parseCode(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();
        // 提取各类代码
        String htmlCode = extractCodeByPattern(codeContent, HTML_CODE_PATTERN);
        String cssCode = extractCodeByPattern(codeContent, CSS_CODE_PATTERN);
        String jsCode = extractCodeByPattern(codeContent, JS_CODE_PATTERN);
        // 设置HTML代码
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        }
        // 设置CSS代码
        if (cssCode != null && !cssCode.trim().isEmpty()) {
            result.setCssCode(cssCode.trim());
        }
        // 设置JS代码
        if (jsCode != null && !jsCode.trim().isEmpty()) {
            result.setJsCode(jsCode.trim());
        }
        return result;
    }
}
