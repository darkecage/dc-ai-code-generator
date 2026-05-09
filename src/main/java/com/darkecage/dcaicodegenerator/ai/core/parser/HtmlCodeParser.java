package com.darkecage.dcaicodegenerator.ai.core.parser;

import com.darkecage.dcaicodegenerator.ai.model.HtmlCodeResult;

import static com.darkecage.dcaicodegenerator.ai.core.utils.ExtractCodeUtils.extractCodeByPattern;
import static com.darkecage.dcaicodegenerator.common.constant.CodeParserPattern.HTML_CODE_PATTERN;

/**
 * HTML 代码解析器
 *
 * @author kaiqi.hu
 */
public class HtmlCodeParser implements CodeParser<HtmlCodeResult>{
    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        // 提取 HTML 代码
        String htmlCode = extractCodeByPattern(codeContent, HTML_CODE_PATTERN);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        }
        return result;
    }

}
