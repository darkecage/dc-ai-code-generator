package com.darkecage.dcaicodegenerator.ai.core;

import com.darkecage.dcaicodegenerator.ai.model.HtmlCodeResult;
import com.darkecage.dcaicodegenerator.ai.model.MultiFileCodeResult;
import com.darkecage.dcaicodegenerator.exception.BusinessException;
import com.darkecage.dcaicodegenerator.exception.ErrorCode;
import com.darkecage.dcaicodegenerator.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成 HTML 模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(result);
    }

    /**
     * 生成多文件模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(result);
    }

    /**
     * 统一入口：根据类型流式生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 流式 AI 响应
     */
    public Flux<String> generateCodeStreamAndSave(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateHtmlCodeStreamAndSave(userMessage);
            case MULTI_FILE -> generateMultiFileCodeStreamAndSave(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 流式生成 HTML 代码，完成后解析并保存文件
     *
     * @param userMessage 用户提示词
     * @return 流式 AI 响应
     */
    private Flux<String> generateHtmlCodeStreamAndSave(String userMessage) {
        StringBuilder fullContent = new StringBuilder();
        return aiCodeGeneratorService.generateHtmlCodeStream(userMessage)
                .doOnNext(fullContent::append)
                .doOnComplete(() -> {
                    try {
                        HtmlCodeResult result = CodeParser.parseHtmlCode(fullContent.toString());
                        File savedDir = CodeFileSaver.saveHtmlCodeResult(result);
                        log.info("HTML 代码生成完成，文件已保存至：{}", savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("HTML 代码解析或保存失败，内容长度：{}", fullContent.length(), e);
                    }
                });
    }

    /**
     * 流式生成多文件代码，完成后解析并保存文件
     *
     * @param userMessage 用户提示词
     * @return 流式 AI 响应
     */
    private Flux<String> generateMultiFileCodeStreamAndSave(String userMessage) {
        StringBuilder fullContent = new StringBuilder();
        return aiCodeGeneratorService.generateMultiFileCodeStream(userMessage)
                .doOnNext(fullContent::append)
                .doOnComplete(() -> {
                    try{
                        MultiFileCodeResult result = CodeParser.parseMultiFileCode(fullContent.toString());
                        File savedDir = CodeFileSaver.saveMultiFileCodeResult(result);
                        log.info("多文件代码生成完成，文件已保存至：{}", savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("多文件代码解析或保存失败，内容长度：{}", fullContent.length(), e);
                    }
                });
    }
}
