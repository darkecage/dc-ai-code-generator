package com.darkecage.dcaicodegenerator.ai.core.parser;

import com.darkecage.dcaicodegenerator.ai.model.CodeParseResult;

/**
 * @Description:
 * @Author: kaiqi.hu
 * @Date: 2026-05-09  15:00
 */
public interface CodeParser<T extends CodeParseResult> {

    T parseCode(String codeContent);
}
