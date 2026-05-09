package com.darkecage.dcaicodegenerator.ai.core.parser;

import com.darkecage.dcaicodegenerator.ai.model.CodeParseResult;
import com.darkecage.dcaicodegenerator.exception.BusinessException;
import com.darkecage.dcaicodegenerator.exception.ErrorCode;
import com.darkecage.dcaicodegenerator.model.enums.CodeGenTypeEnum;

import java.util.Map;

/**
 * 代码解析执行器
 * 根据代码生成类型执行相应的解析逻辑
 *
 * @author kaiqi.hu
 */
public class CodeParserExecutor {

    private static final Map<CodeGenTypeEnum, CodeParser<?>> PARSER_MAP = Map.of(
            CodeGenTypeEnum.HTML, new HtmlCodeParser(),
            CodeGenTypeEnum.MULTI_FILE, new MultiFileCodeParser()
    );

    /**
     * 执行代码解析
     *
     * @param codeContent 代码内容
     * @param codeGenType 代码生成类型
     * @return 解析结果（HtmlCodeResult 或 MultiFileCodeResult）
     */
    public static CodeParseResult executeParser(String codeContent, CodeGenTypeEnum codeGenType) {
        CodeParser<?> parser = PARSER_MAP.get(codeGenType);
        if (parser == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType.name());
        }
        return parser.parseCode(codeContent);
    }
}
