package org.jiwoo.back.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "org.jiwoo.back", annotationClass = Mapper.class)
public class MyBatisConfig {
}
