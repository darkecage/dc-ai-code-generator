package com.darkecage.dcaicodegenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class DcAiCodeGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DcAiCodeGeneratorApplication.class, args);
    }

}
