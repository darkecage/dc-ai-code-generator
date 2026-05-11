package com.darkecage.dcaicodegenerator.ai.core.saver;

import com.darkecage.dcaicodegenerator.exception.BusinessException;
import com.darkecage.dcaicodegenerator.exception.ErrorCode;
import com.darkecage.dcaicodegenerator.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.util.Map;

/**
 * 代码文件保存执行器
 * 根据代码生成类型执行相应的保存逻辑
 *
 * @author kaiqi.hu
 */
public class CodeFileSaverExecutor {

    private static final Map<CodeGenTypeEnum, CodeFileSaverTemplate<?>> SAVER_MAP = Map.of(
            CodeGenTypeEnum.HTML, new HtmlCodeFileSaverTemplate(),
            CodeGenTypeEnum.MULTI_FILE, new MultiFileCodeFileSaverTemplate()
    );

    /**
     * 执行代码保存
     *
     * @param codeResult  代码结果对象
     * @param codeGenType 代码生成类型
     * @return 保存的目录
     */
    @SuppressWarnings("unchecked")
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType) {
        CodeFileSaverTemplate saver = SAVER_MAP.get(codeGenType);
        if (saver == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType.name());
        }
        return saver.saveCode(codeResult);
    }
}
