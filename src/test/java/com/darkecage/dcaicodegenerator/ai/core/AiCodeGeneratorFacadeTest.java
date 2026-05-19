package com.darkecage.dcaicodegenerator.ai.core;

import com.darkecage.dcaicodegenerator.exception.BusinessException;
import com.darkecage.dcaicodegenerator.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("任务记录网站", CodeGenTypeEnum.MULTI_FILE, Long.valueOf("1111"));
        Assertions.assertNotNull(file);
        file = aiCodeGeneratorFacade.generateAndSaveCode("美女网站", CodeGenTypeEnum.HTML, Long.valueOf("1111"));
        Assertions.assertNotNull(file);
    }

    @Test
    void generateCodeStreamAndSave_html() {
        Flux<String> flux = aiCodeGeneratorFacade.generateCodeStreamAndSave("任务记录网站", CodeGenTypeEnum.HTML, Long.valueOf("1111"));
        AtomicBoolean hasContent = new AtomicBoolean(false);
        StepVerifier.create(flux)
                .thenConsumeWhile(chunk -> {
                    hasContent.set(true);
                    return true;
                })
                .verifyComplete();
        Assertions.assertTrue(hasContent.get());
    }

    @Test
    void generateCodeStreamAndSave_multiFile() {
        Flux<String> flux = aiCodeGeneratorFacade.generateCodeStreamAndSave("任务记录网站", CodeGenTypeEnum.MULTI_FILE, Long.valueOf("1111"));
        AtomicBoolean hasContent = new AtomicBoolean(false);
        StepVerifier.create(flux)
                .thenConsumeWhile(chunk -> {
                    hasContent.set(true);
                    return true;
                })
                .verifyComplete();
        Assertions.assertTrue(hasContent.get());
    }

    @Test
    void generateCodeStreamAndSave_nullType() {
        Assertions.assertThrows(BusinessException.class,
                () -> aiCodeGeneratorFacade.generateCodeStreamAndSave("任务记录网站", null, Long.valueOf("1111")));
    }

    @Test
    void generateAndSaveCodeStream_html() {
        Flux<String> flux = aiCodeGeneratorFacade.generateAndSaveCodeStream("简单计算器页面", CodeGenTypeEnum.HTML, Long.valueOf("1111"));
        AtomicBoolean hasContent = new AtomicBoolean(false);
        StepVerifier.create(flux)
                .thenConsumeWhile(chunk -> {
                    hasContent.set(true);
                    return true;
                })
                .verifyComplete();
        Assertions.assertTrue(hasContent.get(), "流式响应应包含内容");
    }

    @Test
    void generateAndSaveCodeStream_multiFile() {
        Flux<String> flux = aiCodeGeneratorFacade.generateAndSaveCodeStream("待办事项应用", CodeGenTypeEnum.MULTI_FILE, Long.valueOf("1111"));
        AtomicBoolean hasContent = new AtomicBoolean(false);
        StepVerifier.create(flux)
                .thenConsumeWhile(chunk -> {
                    hasContent.set(true);
                    return true;
                })
                .verifyComplete();
        Assertions.assertTrue(hasContent.get(), "流式响应应包含内容");
    }

    @Test
    void generateAndSaveCodeStream_nullType() {
        Assertions.assertThrows(BusinessException.class,
                () -> aiCodeGeneratorFacade.generateAndSaveCodeStream("测试", null, Long.valueOf("1111")));
    }
}
