package com.wenzi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wenzi.mapper") //指定 Mapper 接口所在的包路径
public class EvPlatformJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvPlatformJavaApplication.class, args);
    }

}
