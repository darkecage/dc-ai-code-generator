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
        File file = aiCodeGeneratorFacade.generateAndSaveCode("任务记录网站", CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateCodeStreamAndSave_html() {
        Flux<String> flux = aiCodeGeneratorFacade.generateCodeStreamAndSave("任务记录网站", CodeGenTypeEnum.HTML);
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
        Flux<String> flux = aiCodeGeneratorFacade.generateCodeStreamAndSave("任务记录网站", CodeGenTypeEnum.MULTI_FILE);
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
                () -> aiCodeGeneratorFacade.generateCodeStreamAndSave("任务记录网站", null));
    }
}
