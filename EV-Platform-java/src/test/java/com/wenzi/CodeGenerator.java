package com.wenzi;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.sql.Types;
import java.util.Collections;

public class CodeGenerator {

    public static void main(String[] args) {
        // 1. 配置数据库连接信息 (请替换为你自己的密码)
        String url = "jdbc:mysql://localhost:3306/new_energy_vehicle?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "123456";

        // 2. 获取项目根目录 (通常指向 EV-Platform-java)
        String projectPath = System.getProperty("user.dir");

        FastAutoGenerator.create(url, username, password)
                // 全局配置
                .globalConfig(builder -> {
                    builder.author("闻志博") // 设置作者为你自己
                            .enableSwagger() // 开启 swagger 模式，方便后续生成接口文档
                            .outputDir(projectPath + "/src/main/java"); // 指定输出目录到 main/java
                })
                // 包配置
                .packageConfig(builder -> {
                    builder.parent("com.wenzi") // 设置父包名，和你的主类同级
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            // 设置 XML 映射文件的输出路径到 resources/mapper
                            .pathInfo(Collections.singletonMap(OutputFile.xml, projectPath + "/src/main/resources/mapper"));
                })
                // 策略配置 (核心)
                .strategyConfig(builder -> {
                    builder.addInclude("sys_user", "biz_vehicle", "biz_intent_order") // 设置需要生成的表名
                            .addTablePrefix("sys_", "biz_") // 设置过滤表前缀，生成实体类时会自动去掉 sys_ 和 biz_
                            // 实体类配置
                            .entityBuilder()
                            .enableLombok() // 开启 Lombok 注解
                            .enableTableFieldAnnotation() // 开启字段注解
                            // Controller 配置
                            .controllerBuilder()
                            .enableRestStyle(); // 开启 @RestController 风格，适合前后端分离
                })
                // 使用 Velocity 模板引擎
                .templateEngine(new VelocityTemplateEngine())
                .execute();
    }
}
