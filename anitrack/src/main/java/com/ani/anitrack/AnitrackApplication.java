package com.ani.anitrack;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ani.anitrack.mapper")
public class AnitrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnitrackApplication.class, args);
    }
}