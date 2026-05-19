package com.darkecage.dcaicodegenerator.ai.core;

import com.darkecage.dcaicodegenerator.ai.core.parser.CodeParserExecutor;
import com.darkecage.dcaicodegenerator.ai.core.saver.CodeFileSaverExecutor;
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
     * @param appId           应用ID，用于构建唯一目录名
     * @return 流式 AI 响应
     */
    public Flux<String> generateCodeStreamAndSave(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
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

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appId           应用ID，用于构建唯一目录名
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appId           应用ID，用于构建唯一目录名
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }


    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @param appId       应用ID，用于构建唯一目录名
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            // 实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流式返回完成后保存代码
            try {
                String completeCode = codeBuilder.toString();
                // 使用执行器解析代码
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                // 使用执行器保存代码
                File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                log.info("保存成功，路径为：{}", savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }

}
