package com.darkecage.dcaicodegenerator.ai.core.saver;

import com.darkecage.dcaicodegenerator.exception.BusinessException;
import com.darkecage.dcaicodegenerator.exception.ErrorCode;
import com.darkecage.dcaicodegenerator.model.enums.CodeGenTypeEnum;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

/**
 * 代码文件保存执行器
 * 根据代码生成类型执行相应的保存逻辑
 *
 * @author kaiqi.hu
 */
@Component
public class CodeFileSaverExecutor {

    private final Map<CodeGenTypeEnum, CodeFileSaverTemplate<?>> saverMap;

    public CodeFileSaverExecutor(HtmlCodeFileSaverTemplate htmlSaver,
                                  MultiFileCodeFileSaverTemplate multiFileSaver) {
        this.saverMap = Map.of(
                CodeGenTypeEnum.HTML, htmlSaver,
                CodeGenTypeEnum.MULTI_FILE, multiFileSaver
        );
    }

    /**
     * 执行代码保存
     *
     * @param codeResult  代码结果对象
     * @param codeGenType 代码生成类型
     * @param appId       应用ID，用于构建唯一目录名
     * @return 保存的目录
     */
    @SuppressWarnings("unchecked")
    public File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType, Long appId) {
        CodeFileSaverTemplate saver = saverMap.get(codeGenType);
        if (saver == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType.name());
        }
        return saver.saveCode(codeResult, appId);
    }
}