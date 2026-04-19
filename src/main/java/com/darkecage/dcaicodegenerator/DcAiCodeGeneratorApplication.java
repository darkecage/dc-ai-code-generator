package com.darkecage.dcaicodegenerator;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.darkecage.dcaicodegenerator.mapper")
public class DcAiCodeGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DcAiCodeGeneratorApplication.class, args);
    }

}
